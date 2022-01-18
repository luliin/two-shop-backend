package io.luliin.twoshopbackend.service;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.dto.DeletedListResponse;
import io.luliin.twoshopbackend.dto.ModifiedShoppingList;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.Item;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.input.CreateShoppingListInput;
import io.luliin.twoshopbackend.input.HandleCollaboratorInput;
import io.luliin.twoshopbackend.input.ItemInput;
import io.luliin.twoshopbackend.input.ShoppingListItemInput;
import io.luliin.twoshopbackend.messaging.RabbitSender;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ItemRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.graphql.web.webmvc.GraphQlWebSocketHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import reactor.core.Scannable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;


import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class ShoppingListService {

    public final ShoppingListRepository shoppingListRepository;
    private final AppUserRepository appUserRepository;
    private final ItemRepository itemRepository;
    private final SharedService sharedService;

    private Sinks.Many<ShoppingList> shoppingListProcessor;
    private final RabbitSender rabbitSender;


    @PostConstruct
    private void createShoppingListSubscriptions() {
        shoppingListProcessor = Sinks.many().multicast().directBestEffort();
    }

    @PreAuthorize("authentication.principal == #appUser.username or hasAnyRole({'ROLE_ADMIN', 'ROLE_SUPER_ADMIN'})")
    public List<ShoppingList> getOwnedShoppingLists(AppUser appUser) {
        AppUserEntity userEntity = appUserRepository.findById(appUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("No such user found"));
        return shoppingListRepository.findAllByOwner(userEntity);

    }

    @PreAuthorize("authentication.principal == #appUser.username or hasAnyRole({'ROLE_ADMIN', 'ROLE_SUPER_ADMIN'})")
    public List<ShoppingList> getCollaboratorShoppingLists(AppUser appUser) {
        AppUserEntity userEntity = appUserRepository.findById(appUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("No such user found"));
        return shoppingListRepository.findAllByCollaborator(userEntity);
    }

    @PreAuthorize("isAuthenticated()")
    public ShoppingList createShoppingList(CreateShoppingListInput createShoppingListInput, String username) {

        AppUserEntity owner = sharedService.getUser(username,
                "Unable to fetch user information when creating shopping list");

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        ShoppingList newShoppingList = ShoppingList.builder()
                .name(createShoppingListInput.name())
                .owner(owner)
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .build();

        if (appUserRepository.existsByUsernameOrEmail(
                createShoppingListInput.collaboratorCredential(),
                createShoppingListInput.collaboratorCredential())
        ) {
            log.info("Collaborator present");
            newShoppingList.setCollaborator(appUserRepository.findByUsernameOrEmail(
                    createShoppingListInput.collaboratorCredential(),
                    createShoppingListInput.collaboratorCredential())
                    .orElseThrow(() -> new RuntimeException("An unexpected error occurred while adding collaborator")));
        }
        return shoppingListRepository.save(newShoppingList);

    }

    @PreAuthorize("isAuthenticated()")
    public ShoppingList modifyShoppingListItems(Long itemId,
                                                Boolean removeItem,
                                                @Valid ShoppingListItemInput shoppingListItemInput) {
        ShoppingList shoppingList = sharedService.shoppingListById(
                shoppingListItemInput.shoppingListId(),
                "You are not authorized to modify this shopping list");

        log.info("Modifying shopping list {} ", shoppingList.getName());


        if (removeItem != null && removeItem) {
            return removeItem(shoppingList, itemId);
        } else if (itemId != null) {
            return updateItem(shoppingList, itemId, shoppingListItemInput.itemInput());
        } else {
            return addItem(shoppingList, shoppingListItemInput.itemInput());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("returnObject.owner.username == authentication.principal " +
            "or returnObject.collaborator != null and returnObject.collaborator.username == authentication.principal")
    private ShoppingList updateItem(ShoppingList shoppingList, Long itemId, @Valid ItemInput input) {
        if (input == null) throw new IllegalArgumentException("Cannot update item without item input");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("No such item"));
        if (shoppingList != item.getShoppingList())
            throw new IllegalArgumentException("The item does not belong to this shopping list");
        log.info("Updating item with id {}", item.getId());
        item.setName((input.name() != null) ? input.name().trim() : item.getName());
        item.setQuantity((input.quantity() != null) ? input.quantity() : item.getQuantity());
        item.setUnit((input.unit() != null) ? input.unit() : item.getUnit());
        item.setIsCompleted((input.isCompleted() != null) ? input.isCompleted() : item.getIsCompleted());

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        ShoppingList updatedShoppingList = itemRepository.save(item).getShoppingList();
        updatedShoppingList.setUpdatedAt(now);

        final ShoppingList savedList = shoppingListRepository.save(updatedShoppingList);
        rabbitSender.publishShoppingListSubscription(savedList);

        return savedList;
    }

    @PostAuthorize("returnObject.owner.username == authentication.principal " +
            "or returnObject.collaborator != null and returnObject.collaborator.username == authentication.principal")
    private ShoppingList removeItem(ShoppingList shoppingList, Long itemId) {
        if (itemId == null) throw new IllegalArgumentException("Can not delete item. Id was null.");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("No such item"));
        shoppingList.removeItem(item);
        log.info("Removing item with id {}", item.getId());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        shoppingList.setUpdatedAt(now);
        final ShoppingList updatedList = shoppingListRepository.save(shoppingList);
        rabbitSender.publishShoppingListSubscription(updatedList);
        return updatedList;
    }

    @PostAuthorize("returnObject.owner.username == authentication.principal " +
            "or returnObject.collaborator != null and returnObject.collaborator.username == authentication.principal")
    private ShoppingList addItem(ShoppingList shoppingList,
                                @Valid ItemInput input) {

        Item item = Item.builder()
                .name(input.name().trim())
                .quantity(input.quantity())
                .unit(input.unit())
                .isCompleted((input.isCompleted() != null) ? input.isCompleted() : false)
                .build();

        shoppingList.addItem(item);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        shoppingList.setUpdatedAt(now);
        log.info("Adding new item with to shopping list with id {}", shoppingList.getId());
        final ShoppingList updatedList = shoppingListRepository.save(shoppingList);
        rabbitSender.publishShoppingListSubscription(updatedList);
        return updatedList;
    }


    @RabbitListener(queues = "#{queue1.name}")
    public void publish(Long shoppingListId) {
        ShoppingList shoppingList = shoppingListRepository
                .findById(shoppingListId)
                .orElse(null);
        if (shoppingList != null) {
            log.info(" >>> ShoppingListService : Shopping list {} updated", shoppingListId);
            shoppingListProcessor.tryEmitNext(shoppingList);
        } else {
            log.info(" >>> There is no shopping list with id: {}", shoppingListId);
        }

    }


    public Flux<List<Item>> getShoppingListPublisher(Long shoppingListId, DataFetchingEnvironment environment) {
        log.info("In getShoppingListPublisher {}", shoppingListId);
        environment.getArguments().forEach((a, b) -> log.info("Arguments: {}={}", a, b));

        return shoppingListProcessor.asFlux()
                .filter(shoppingList -> shoppingListId.equals(shoppingList.getId()))
                .map(shoppingList -> {
                    log.info("Publishing individual subscription update for Shopping list {}", shoppingList.getName());
                    return shoppingList.getItems();
                }).log();
    }


    @PreAuthorize("isAuthenticated()")
    public ModifiedShoppingList addCollaborator(HandleCollaboratorInput handleCollaboratorInput, String username) {

        AppUserEntity owner = sharedService.getUser(username, "An unexpected error occurred. " +
                "Could not fetch user information from authenticated user");

        if (!shoppingListRepository.existsByIdAndOwner(handleCollaboratorInput.shoppingListId(), owner)) {
            log.error("Only the owner of a shopping list may add a collaborator");
            throw new AccessDeniedException("Only the owner of a shopping list may add a collaborator");
        }

        ShoppingList shoppingList = sharedService.shoppingListById(handleCollaboratorInput.shoppingListId(),
                "An unexpected error occurred.");

        if (shoppingList.getCollaborator() != null) {
            throw new IllegalArgumentException("There is already a collaborator on this list. Please remove them first.");
        }

        AppUserEntity collaborator =
                sharedService.getUserFromUsernameOrEmail(
                        handleCollaboratorInput.collaboratorCredential(),
                        "No such user");

        if (owner.equals(collaborator)) {
            throw new IllegalArgumentException("You can't add yourself as a collaborator");
        }

        ShoppingList savedList = shoppingListRepository.save(shoppingList.setCollaborator(collaborator));

        return savedList.toModifiedShoppingList(collaborator.getUsername() + " has been added as a collaborator");
    }


    @PreAuthorize("isAuthenticated()")
    public ModifiedShoppingList removeCollaborator(HandleCollaboratorInput handleCollaboratorInput) {

        if (!shoppingListRepository.existsById(handleCollaboratorInput.shoppingListId())) {
            throw new IllegalArgumentException("No such shopping list");
        }

        sharedService.shoppingListById(handleCollaboratorInput.shoppingListId(),
                "You are not authorized to modify this shopping list");

        ShoppingList shoppingList = sharedService.shoppingListById(handleCollaboratorInput.shoppingListId(),
                "An unexpected error occurred.");

        if (shoppingList.getCollaborator() == null) {
            throw new IllegalArgumentException("There is no collaborator to remove on this list.");
        }
        ShoppingList savedList = shoppingListRepository.save(shoppingList.setCollaborator(null));

        return savedList.toModifiedShoppingList(handleCollaboratorInput.collaboratorCredential()
                + " has been removed as a collaborator");
    }

    @PreAuthorize("isAuthenticated()")
    public ModifiedShoppingList changeShoppingListName(Long shoppingListId,
                                                       @NotBlank(message = "Ogiltigt namn på shoppinglistan")
                                                       @Length(max = 75,
                                                               message = "Namnet får inte innehålla mer än {max} tecken")
                                                               String newName) {

        ShoppingList shoppingList = sharedService.shoppingListById(shoppingListId,
                "You're not authorized to modify the name of this shopping list");

        ShoppingList savedList = shoppingListRepository.save(shoppingList.setName(newName.trim()));

        return savedList.toModifiedShoppingList("You successfully changed the name to " + newName.trim());

    }

    @PreAuthorize("isAuthenticated()")
    public ModifiedShoppingList clearAllItems(Long shoppingListId) {
        ShoppingList shoppingList = sharedService.shoppingListById(shoppingListId,
                "You are not authorized to modify this shopping list");

        shoppingList.removeAllItems();
        ShoppingList updatedList = shoppingListRepository.save(shoppingList);
        rabbitSender.publishShoppingListSubscription(updatedList);
        return updatedList.toModifiedShoppingList(updatedList.getName()+ " är nu tom!");
    }

    @PreAuthorize("isAuthenticated()")
    public DeletedListResponse deleteShoppingList(Long shoppingListId) {
        ShoppingList shoppingList = sharedService.ownedShoppingListById(shoppingListId,
                "You are not allowed to modify this shopping list");

        shoppingListRepository.delete(shoppingList);
        shoppingList.setItems(null);
        shoppingListProcessor.tryEmitNext(shoppingList);

        return new DeletedListResponse(shoppingList.getOwner().getUsername() + " tog bort " + shoppingList.getName(),
                "/home");

    }


//TODO: Add functionality to delete a shopping list (making sure all items are deleted as well) Implement second subscription

}

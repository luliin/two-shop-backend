package io.luliin.twoshopbackend.service;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.Item;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.input.CreateShoppingListInput;
import io.luliin.twoshopbackend.input.ItemInput;
import io.luliin.twoshopbackend.input.ShoppingListItemInput;
import io.luliin.twoshopbackend.messaging.RabbitSender;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ItemRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListService {

    public final ShoppingListRepository shoppingListRepository;
    private final AppUserRepository appUserRepository;
    private final ItemRepository itemRepository;

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

    public List<ShoppingList> getCollaboratorShoppingLists(AppUser appUser) {
        AppUserEntity userEntity = appUserRepository.findById(appUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("No such user found"));
        return shoppingListRepository.findAllByCollaborator(userEntity);
    }

    public ShoppingList createShoppingList(CreateShoppingListInput createShoppingListInput) {

        AppUserEntity owner = appUserRepository.findByUsernameOrEmail(createShoppingListInput.ownerCredential(), createShoppingListInput.ownerCredential())
                .orElseThrow(() -> new IllegalArgumentException("Trying to create shopping list without valid owner"));

        if (shoppingListRepository.existsByOwnerAndName(owner, createShoppingListInput.name())) {
            throw new IllegalArgumentException("You can't own multiple shopping lists with the same name");
        }
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        ShoppingList newShoppingList = ShoppingList.builder()
                .name(createShoppingListInput.name())
                .owner(owner)
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .build();

        //TODO : Clean up
        if (appUserRepository.existsByUsernameOrEmail(createShoppingListInput.collaboratorCredential(), createShoppingListInput.collaboratorCredential())) {
            log.info("Collaborator present");
            newShoppingList.setCollaborator(appUserRepository.findByUsernameOrEmail(
                    createShoppingListInput.collaboratorCredential(),
                    createShoppingListInput.collaboratorCredential()).get());
        }
        return shoppingListRepository.save(newShoppingList);

    }

    public ShoppingList modifyShoppingListItems(Long itemId, Boolean removeItem, ShoppingListItemInput shoppingListItemInput) {
        log.info("Trying to modify request with item id {}, remove item = {}", itemId, removeItem);
        log.info("ShoppingListInput = {}", shoppingListItemInput.toString());
        AppUserEntity ownerOrCollaborator = appUserRepository.findByUsernameOrEmail(shoppingListItemInput.userCredentials(), shoppingListItemInput.userCredentials())
                .orElseThrow(() -> new IllegalArgumentException("Trying to create shopping list without valid owner"));

        ShoppingList shoppingList = shoppingListRepository.findByIdAndOwnerOrIdAndCollaborator(
                shoppingListItemInput.shoppingListId(),
                ownerOrCollaborator,
                shoppingListItemInput.shoppingListId(),
                ownerOrCollaborator)
                .orElseThrow(() -> new IllegalArgumentException("You are not authorized to modify this shopping list"));

        log.info("Modifying shopping list {} ", shoppingList.getName());


        if (removeItem != null && removeItem) {
            return removeItem(shoppingList, itemId);
        } else if (itemId != null) {
            return updateItem(shoppingList, itemId, shoppingListItemInput.itemInput());
        } else {
            return addItem(shoppingList, shoppingListItemInput.itemInput());
        }
    }

    private ShoppingList updateItem(ShoppingList shoppingList, Long itemId, ItemInput input) {
        if(input==null) throw new IllegalArgumentException("Cannot update item without item input");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("No such item"));
        if (shoppingList != item.getShoppingList())
            throw new IllegalArgumentException("The item does not belong to this shopping list");
        log.info("Updating item with id {}", item.getId());
        item.setName((input.name() != null) ? input.name() : item.getName());
        item.setQuantity((input.quantity() != null) ? input.quantity() : item.getQuantity());
        item.setUnit((input.unit() != null) ? input.unit() : item.getUnit());
        item.setIsCompleted((input.isCompleted() != null) ? input.isCompleted() : item.getIsCompleted());

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        ShoppingList updatedShoppingList = itemRepository.save(item).getShoppingList();
        updatedShoppingList.setUpdatedAt(now);

        final ShoppingList savedList = shoppingListRepository.save(updatedShoppingList);
//        publish(savedList);
        rabbitSender.publishShoppingListSubscription(savedList);

        return savedList;
    }

    private ShoppingList removeItem(ShoppingList shoppingList, Long itemId) {
        if (itemId == null) throw new IllegalArgumentException("Can not delete item. Id was null.");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("No such item"));
        shoppingList.removeItem(item);
        log.info("Removing item with id {}", item.getId());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        shoppingList.setUpdatedAt(now);
        final ShoppingList updatedList = shoppingListRepository.save(shoppingList);
//        publish(updatedList);
        rabbitSender.publishShoppingListSubscription(updatedList);
        return updatedList;
    }

    private ShoppingList addItem(ShoppingList shoppingList, ItemInput input) {
        Item item = Item.builder()
                .name(input.name())
                .quantity(input.quantity())
                .unit(input.unit())
                .isCompleted((input.isCompleted() != null) ? input.isCompleted() : false)
                .build();

        shoppingList.addItem(item);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        shoppingList.setUpdatedAt(now);
        log.info("Adding new item with to shopping list with id {}", shoppingList.getId());
        final ShoppingList updatedList = shoppingListRepository.save(shoppingList);
//        publish(updatedList);
        rabbitSender.publishShoppingListSubscription(updatedList);
        return updatedList;
    }

    @RabbitListener(queues = "#{queue1.name}")
    public void publish(Long shoppingListId) {
        ShoppingList shoppingList = shoppingListRepository
                .findById(shoppingListId)
                .orElse(null);
        if(shoppingList!= null)  {
            log.info(" >>> ShoppingListService : Shopping list {} updated", shoppingListId);
            shoppingListProcessor.tryEmitNext(shoppingList);
        } else {
            log.info(" >>> There is no shopping list with id: {}", shoppingListId);
        }

    }

    public Flux<List<Item>> getShoppingListPublisher(Long shoppingListId) {
        log.info("In getShoppingListPublisher {}", shoppingListId);
        return shoppingListProcessor.asFlux()
                .filter(shoppingList -> shoppingListId.equals(shoppingList.getId()))
                .map(shoppingList -> {
                    log.info("Publishing individual subscription update for Shopping list {}", shoppingList.getName());
                    return shoppingList.getItems();
                });
    }

    public ShoppingList getShoppingListById(Long shoppingListId) {
        return shoppingListRepository.findById(shoppingListId).orElse(null);
    }
}

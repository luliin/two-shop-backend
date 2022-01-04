package io.luliin.twoshopbackend.service;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.Item;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.input.CreateShoppingListInput;
import io.luliin.twoshopbackend.input.ItemInput;
import io.luliin.twoshopbackend.input.ShoppingListItemInput;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ItemRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public List<ShoppingList> getOwnedShoppingLists(Long userId) {
        AppUserEntity userEntity = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No such user found"));
        return shoppingListRepository.findAllByOwner(userEntity);

    }

    public List<ShoppingList> getCollaboratorShoppingLists(Long userId) {
        AppUserEntity userEntity = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No such user found"));
        return shoppingListRepository.findAllByCollaborator(userEntity);
    }

    public ShoppingList createShoppingList(CreateShoppingListInput createShoppingListInput) {

        AppUserEntity owner = appUserRepository.findByUsernameOrEmail(createShoppingListInput.ownerCredential(), createShoppingListInput.ownerCredential())
                .orElseThrow(() -> new IllegalArgumentException("Trying to create shopping list without valid owner"));

        if(shoppingListRepository.existsByOwnerAndName(owner, createShoppingListInput.name())) {
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
        if(appUserRepository.existsByUsernameOrEmail(createShoppingListInput.collaboratorCredential(), createShoppingListInput.collaboratorCredential())) {
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



        if(removeItem != null) {
            return removeItem(shoppingList, itemId);
        } else if(itemId != null) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ShoppingList updatedShoppingList = updateItem(shoppingList, itemId, shoppingListItemInput.itemInput());
            updatedShoppingList.setUpdatedAt(now);
            return shoppingListRepository.save(updatedShoppingList);
        } else {
            return addItem(shoppingList, shoppingListItemInput.itemInput());
        }
    }

    private ShoppingList updateItem(ShoppingList shoppingList, Long itemId, ItemInput input) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("No such item"));
        if(shoppingList != item.getShoppingList()) throw new IllegalArgumentException("The item does not belong to this shopping list");
        log.info("Updating item with id {}", item.getId());
        item.setName((input.name() != null) ? input.name() : item.getName());
        item.setQuantity((input.quantity() != null) ? input.quantity() : item.getQuantity());
        item.setUnit((input.unit() != null) ? input.unit() : item.getUnit());
        item.setIsCompleted((input.isCompleted() != null) ? input.isCompleted() : item.getIsCompleted());
        return itemRepository.save(item).getShoppingList();
    }

    private ShoppingList removeItem(ShoppingList shoppingList, Long itemId) {
        if(itemId == null) throw new IllegalArgumentException("Can not delete item. Id was null.");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("No such item"));
        shoppingList.removeItem(item);
        log.info("Removing item with id {}", item.getId());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        shoppingList.setUpdatedAt(now);
        return shoppingListRepository.save(shoppingList);
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
        return shoppingListRepository.save(shoppingList);
    }


}
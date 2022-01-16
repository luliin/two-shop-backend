package io.luliin.twoshopbackend.controller;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.dto.ModifiedShoppingList;
import io.luliin.twoshopbackend.entity.Item;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.input.CreateShoppingListInput;
import io.luliin.twoshopbackend.input.HandleCollaboratorInput;
import io.luliin.twoshopbackend.input.ShoppingListItemInput;
import io.luliin.twoshopbackend.service.SharedService;
import io.luliin.twoshopbackend.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final SharedService sharedService;

    @SchemaMapping(typeName = "AppUser")
    public List<ShoppingList> ownedShoppingLists(AppUser appUser) {
        return shoppingListService.getOwnedShoppingLists(appUser);
    }

    @SchemaMapping(typeName = "AppUser")
    public List<ShoppingList> collaboratorShoppingLists(AppUser appUser) {
        return shoppingListService.getCollaboratorShoppingLists(appUser);
    }

    @QueryMapping
    public ShoppingList shoppingListById(@Argument Long shoppingListId) {
        return sharedService.shoppingListById(shoppingListId, "No such shopping list");
    }

    @MutationMapping
    public ShoppingList createShoppingList(@Valid @Argument CreateShoppingListInput createShoppingListInput,
                                           Principal principal) {
        return shoppingListService.createShoppingList(createShoppingListInput, principal.getName());
    }

    @MutationMapping
    public ShoppingList modifyShoppingListItems(@Argument Long itemId,
                                                @Argument Boolean removeItem,
                                                @Valid @Argument ShoppingListItemInput shoppingListItemInput) {
        return shoppingListService.modifyShoppingListItems(itemId, removeItem, shoppingListItemInput);
    }

    @MutationMapping
    public ModifiedShoppingList inviteCollaborator(@Valid @Argument HandleCollaboratorInput handleCollaboratorInput,
                                                   Principal principal) {
        return shoppingListService.addCollaborator(handleCollaboratorInput, principal.getName());
    }

    @MutationMapping
    public ModifiedShoppingList removeCollaborator(@Valid @Argument HandleCollaboratorInput handleCollaboratorInput,
                                                   Principal principal) {
        return shoppingListService.removeCollaborator(handleCollaboratorInput, principal.getName());
    }

    @SubscriptionMapping
    public Publisher<List<Item>> itemModified(@Argument Long shoppingListId) {
        log.info("In subscription mapping for shoppingListId {}", shoppingListId);
        return shoppingListService.getShoppingListPublisher(shoppingListId);
    }

}

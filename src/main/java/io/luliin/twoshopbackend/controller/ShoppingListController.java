package io.luliin.twoshopbackend.controller;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.input.CreateShoppingListInput;
import io.luliin.twoshopbackend.input.ShoppingListItemInput;
import io.luliin.twoshopbackend.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Controller
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @SchemaMapping(typeName = "AppUser")
    public List<ShoppingList> ownedShoppingLists(AppUser appUser) {
        return shoppingListService.getOwnedShoppingLists(appUser.getId());
    }

    @SchemaMapping(typeName = "AppUser")
    public List<ShoppingList> collaboratorShoppingLists(AppUser appUser) {
        return shoppingListService.getCollaboratorShoppingLists(appUser.getId());
    }

    @MutationMapping
    public ShoppingList createShoppingList(@Valid @Argument CreateShoppingListInput createShoppingListInput) {
        return shoppingListService.createShoppingList(createShoppingListInput);
    }

    @MutationMapping
    public ShoppingList modifyShoppingListItems(@Argument Long itemId,
                                                @Argument Boolean removeItem,
                                                @Valid @Argument ShoppingListItemInput shoppingListItemInput) {
        return shoppingListService.modifyShoppingListItems(itemId, removeItem, shoppingListItemInput);
    }

}

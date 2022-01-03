package io.luliin.twoshopbackend.service;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    public final ShoppingListRepository shoppingListRepository;
    private final AppUserRepository appUserRepository;

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
}

package io.luliin.twoshopbackend.service;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SharedService {

    private final AppUserRepository appUserRepository;
    private final ShoppingListRepository shoppingListRepository;

    @PreAuthorize("isAuthenticated()")
    public AppUserEntity getUser(String username, String errorMessage) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }

    @PreAuthorize("isAuthenticated()")
    public AppUserEntity getUserFromUsernameOrEmail(String credential, String errorMessage) {
        return appUserRepository.
                findByUsernameOrEmail(credential,
                        credential)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole({'ROLE_ADMIN', 'ROLE_SUPER_ADMIN'}) or returnObject.owner.username == authentication.principal " +
            "or returnObject.collaborator != null and returnObject.collaborator.username == authentication.principal")
    public ShoppingList shoppingListById(Long shoppingListId, String errorMessage) {
        return shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }


    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole({'ROLE_ADMIN', 'ROLE_SUPER_ADMIN'}) or returnObject.owner.username == authentication.principal")
    public ShoppingList ownedShoppingListById(Long shoppingListId, String errorMessage) {
        return shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }
}

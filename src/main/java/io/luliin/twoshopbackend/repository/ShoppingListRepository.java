package io.luliin.twoshopbackend.repository;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findAllByOwner(AppUserEntity owner);
    List<ShoppingList> findAllByCollaborator(AppUserEntity collaborator);
}

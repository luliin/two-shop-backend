package io.luliin.twoshopbackend.repository;

import io.luliin.twoshopbackend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Julia Wigenstedt
 * Date: 2022-01-04
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
}

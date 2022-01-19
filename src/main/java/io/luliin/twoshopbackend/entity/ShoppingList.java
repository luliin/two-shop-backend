package io.luliin.twoshopbackend.entity;

import io.luliin.twoshopbackend.dto.ModifiedShoppingList;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ShoppingList is the Java representation of the PostgreSQL table "shopping_list".
 * This is also the POJO equivalent of the GraphQL type ShoppingList.
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Accessors(chain = true)
public class ShoppingList {
    @SequenceGenerator(
            name = "shopping_list_id_sequence",
            sequenceName = "shopping_list_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "shopping_list_id_sequence"
    )
    @Id
    private Long id;
    @Column(nullable = false)
    private String name;
    @ManyToOne
    private AppUserEntity owner;
    @ManyToOne
    private AppUserEntity collaborator;
    @Column(nullable = false)
    private Timestamp createdAt;
    @Column(nullable = false)
    private Timestamp updatedAt;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "shoppingList", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SELECT)
    @OrderBy("id ASC")
    List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
        item.setShoppingList(this);
    }

    public void removeItem(Item item) {
        if ((item.getShoppingList() != this)) {
            throw new IllegalArgumentException("The item does not belong to this shopping list");
        }
        items.remove(item);
        item.setShoppingList(null);
    }

    public ModifiedShoppingList toModifiedShoppingList(String message) {
        return new ModifiedShoppingList(this, message);
    }

    public void removeAllItems() {
        items.clear();
    }
}

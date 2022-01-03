package io.luliin.twoshopbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
    @ManyToOne
    private AppUserEntity owner;
    @ManyToOne
    private AppUserEntity collaborator;
    @Column(nullable = false)
    private Timestamp createdAt;
    @Column(nullable = false)
    private Timestamp updatedAt;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "shoppingList")
    List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
        item.setShoppingList(this);
    }

    public void removeItem(Item item) {
        items.remove(item);
        item.setShoppingList(null);
    }
}

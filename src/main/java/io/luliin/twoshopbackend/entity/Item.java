package io.luliin.twoshopbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 * Item is the Java representation of the PostgreSQL table "item".
 * This is also the POJO equivalent of the GraphQL type Item.
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Builder
public class Item {

    @SequenceGenerator(
            name = "item_id_sequence",
            sequenceName = "item_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "item_id_sequence"
    )
    @Id
    private Long id;
    @NotBlank
    private String name;
    private Double quantity;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    @ManyToOne(optional = false)
    private ShoppingList shoppingList;


    public enum Unit {
        ST,
        CL,
        DL,
        L,
        G,
        HG,
        KG
    }
}

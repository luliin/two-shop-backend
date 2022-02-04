package io.luliin.twoshopbackend.entity;

import lombok.*;

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
@Getter
@Builder
@ToString
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
    @NotBlank(message = "Ogiltigt namn på produkten")
    private String name;
    private Double quantity;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    private Boolean isCompleted;
    @ManyToOne(optional = false)
    @ToString.Exclude
    private ShoppingList shoppingList;


    /**
     * Available units
     */
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

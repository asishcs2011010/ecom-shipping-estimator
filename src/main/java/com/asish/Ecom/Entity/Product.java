package com.asish.Ecom.Entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "selling_price", nullable = false)
    private Long sellingPrice;

    @Column(nullable = false)
    private Double weightKg;

    @Column(nullable = false)
    private Double dimensionLength;

    @Column(nullable = false)
    private Double dimensionWidth;

    @Column(nullable = false)
    private Double dimensionHeight;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;
}

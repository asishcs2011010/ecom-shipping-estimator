package com.asish.Ecom.Config;

import com.asish.Ecom.Entity.Customer;
import com.asish.Ecom.Entity.Product;
import com.asish.Ecom.Entity.Seller;
import com.asish.Ecom.Entity.Warehouse;
import com.asish.Ecom.Repository.CustomerRepository;
import com.asish.Ecom.Repository.ProductRepository;
import com.asish.Ecom.Repository.SellerRepository;
import com.asish.Ecom.Repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {

            if (sellerRepository.count() == 0) {
                log.info("Seeding sellers...");

                Seller nestleSeller = sellerRepository.save(
                        Seller.builder()
                                .name("Nestle Seller")
                                .latitude(20.5937)   // Central India
                                .longitude(78.9629)
                                .build()
                );

                Seller riceSeller = sellerRepository.save(
                        Seller.builder()
                                .name("Rice Seller")
                                .latitude(28.6139)   // Delhi
                                .longitude(77.2090)
                                .build()
                );

                Seller sugarSeller = sellerRepository.save(
                        Seller.builder()
                                .name("Sugar Seller")
                                .latitude(19.0760)   // Mumbai
                                .longitude(72.8777)
                                .build()
                );

                log.info("Sellers seeded: {}, {}, {}", nestleSeller.getName(),
                        riceSeller.getName(), sugarSeller.getName());

                // ---------------------------------------------------------
                // PRODUCTS  (PDF: Maggie 500g, Rice Bag 10Kg, Sugar Bag 25Kg)
                //   Dimensions from PDF are in cm (length x width x height)
                // ---------------------------------------------------------
                if (productRepository.count() == 0) {
                    log.info("Seeding products...");

                    productRepository.save(
                            Product.builder()
                                    .productName("Maggie 500g Packet")
                                    .sellingPrice(10L)          // Rs 10
                                    .weightKg(0.5)              // 0.5 kg
                                    .dimensionLength(10.0)      // 10cm x 10cm x 10cm
                                    .dimensionWidth(10.0)
                                    .dimensionHeight(10.0)
                                    .seller(nestleSeller)
                                    .build()
                    );

                    productRepository.save(
                            Product.builder()
                                    .productName("Rice Bag 10Kg")
                                    .sellingPrice(500L)         // Rs 500
                                    .weightKg(10.0)             // 10 kg
                                    .dimensionLength(1000.0)    // 1000cm x 800cm x 500cm
                                    .dimensionWidth(800.0)
                                    .dimensionHeight(500.0)
                                    .seller(riceSeller)
                                    .build()
                    );

                    productRepository.save(
                            Product.builder()
                                    .productName("Sugar Bag 25Kg")
                                    .sellingPrice(700L)         // Rs 700
                                    .weightKg(25.0)             // 25 kg
                                    .dimensionLength(1000.0)    // 1000cm x 900cm x 600cm
                                    .dimensionWidth(900.0)
                                    .dimensionHeight(600.0)
                                    .seller(sugarSeller)
                                    .build()
                    );

                    log.info("Products seeded.");
                }
            }

            // ---------------------------------------------------------
            // CUSTOMERS  (PDF: Cust-123 Shree Kirana Store, Cust-124 Andheri Mini Mart)
            // ---------------------------------------------------------
            if (customerRepository.count() == 0) {
                log.info("Seeding customers...");

                customerRepository.save(
                        Customer.builder()
                                .customerId("Cust-123")
                                .name("Shree Kirana Store")
                                .phoneNumber("9847000000")   // last digits masked in PDF
                                .latitude(11.232)
                                .longitude(23.445495)
                                .build()
                );

                customerRepository.save(
                        Customer.builder()
                                .customerId("Cust-124")
                                .name("Andheri Mini Mart")
                                .phoneNumber("9101000000")   // last digits masked in PDF
                                .latitude(17.232)
                                .longitude(33.445495)
                                .build()
                );

                log.info("Customers seeded.");
            }

            // ---------------------------------------------------------
            // WAREHOUSES  (PDF: BLR_Warehouse, MUMB_Warehouse)
            // ---------------------------------------------------------
            if (warehouseRepository.count() == 0) {
                log.info("Seeding warehouses...");

                warehouseRepository.save(
                        Warehouse.builder()
                                .name("BLR_Warehouse")
                                .latitude(12.99999)
                                .longitude(37.923273)
                                .build()
                );

                warehouseRepository.save(
                        Warehouse.builder()
                                .name("MUMB_Warehouse")
                                .latitude(11.99999)
                                .longitude(27.923273)
                                .build()
                );

                log.info("Warehouses seeded.");
            }

            log.info("Database seeding complete.");
        };
    }
}
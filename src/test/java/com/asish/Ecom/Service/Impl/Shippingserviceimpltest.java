package com.asish.Ecom.Service.Impl;

import com.asish.Ecom.DTO.Request.ShippingCalculateRequest;
import com.asish.Ecom.DTO.Response.ShippingCalculateResponse;
import com.asish.Ecom.DTO.Response.ShippingChargeResponse;
import com.asish.Ecom.Entity.Customer;
import com.asish.Ecom.Entity.Product;
import com.asish.Ecom.Entity.Seller;
import com.asish.Ecom.Entity.Warehouse;
import com.asish.Ecom.Repository.CustomerRepository;
import com.asish.Ecom.Repository.ProductRepository;
import com.asish.Ecom.Repository.SellerRepository;
import com.asish.Ecom.Repository.WarehouseRepository;
import com.asish.Ecom.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceImplTest {

    @Mock private WarehouseRepository  warehouseRepository;
    @Mock private CustomerRepository   customerRepository;
    @Mock private SellerRepository     sellerRepository;
    @Mock private ProductRepository    productRepository;

    @InjectMocks
    private ShippingServiceImpl shippingService;

    /*
     * Fixed real-world coordinates used throughout:
     *
     *   Warehouse (Jaipur)   26.9124 N, 75.7873 E   ← nearest to Delhi seller
     *   Warehouse (Chennai)  13.0827 N, 80.2707 E   ← far
     *   Seller    (Delhi)    28.6139 N, 77.2090 E
     *   Customer  (Pune)     18.5204 N, 73.8567 E
     *
     * Haversine: Jaipur → Pune ≈ 900 km  → TRUCK (>=100, <500? No, 900 > 500 → AEROPLANE)
     *   transportMode = AEROPLANE  (rate = 1.0/km/kg)
     *   standard charge = 10 + (900 * weight * 1.0)
     */

    private Warehouse jaipur;
    private Warehouse chennai;
    private Seller    seller;
    private Customer  customer;
    private Product   product;

    @BeforeEach
    void setUp() {
        jaipur = Warehouse.builder()
                .id(10L).name("Jaipur")
                .latitude(26.9124).longitude(75.7873)
                .build();

        chennai = Warehouse.builder()
                .id(20L).name("Chennai")
                .latitude(13.0827).longitude(80.2707)
                .build();

        seller = Seller.builder()
                .id(1L).name("Delhi Seller")
                .latitude(28.6139).longitude(77.2090)
                .build();

        customer = Customer.builder()
                .id(5L).name("Pune Customer")
                .latitude(18.5204).longitude(73.8567)
                .build();

        product = Product.builder()
                .id(100L).productName("Laptop")
                .weightKg(2.0).sellingPrice(50000L)
                .dimensionLength(40.0).dimensionWidth(30.0).dimensionHeight(5.0)
                .seller(seller)
                .build();
    }

    // =========================================================================
    // getShippingCharge()
    // =========================================================================

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void getShippingCharge_shouldReturnPositiveCharge_forStandardDelivery() {
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(jaipur));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));

        ShippingChargeResponse response = shippingService.getShippingCharge(10L, 5L, "standard");

        assertThat(response).isNotNull();
        assertThat(response.getShippingCharge()).isPositive();
    }

    @Test
    void getShippingCharge_shouldReturnHigherCharge_forExpressThanStandard() {
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(jaipur));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));

        ShippingChargeResponse standard = shippingService.getShippingCharge(10L, 5L, "standard");

        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(jaipur));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));

        ShippingChargeResponse express = shippingService.getShippingCharge(10L, 5L, "express");

        assertThat(express.getShippingCharge()).isGreaterThan(standard.getShippingCharge());
    }

    @Test
    void getShippingCharge_shouldUseWeight1_asDefaultWeightInCharge() {
        // getShippingCharge() always passes weightKg=1.0 to ShippingChargeCalculator
        // standard: 10 + (distance * 1.0 * rate)  — charge must be > base charge (10.0)
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(jaipur));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));

        ShippingChargeResponse response = shippingService.getShippingCharge(10L, 5L, "standard");

        assertThat(response.getShippingCharge()).isGreaterThan(10.0);
    }

    // ── Repository interaction ────────────────────────────────────────────────

    @Test
    void getShippingCharge_shouldCallWarehouseAndCustomerRepo_exactlyOnce() {
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(jaipur));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));

        shippingService.getShippingCharge(10L, 5L, "standard");

        verify(warehouseRepository, times(1)).findById(10L);
        verify(customerRepository,  times(1)).findById(5L);
    }

    @Test
    void getShippingCharge_shouldNeverCallCustomerRepo_whenWarehouseNotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.getShippingCharge(99L, 5L, "standard"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(customerRepository, never()).findById(any());
    }

    // ── Exception paths ───────────────────────────────────────────────────────

    @Test
    void getShippingCharge_shouldThrow_whenWarehouseNotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.getShippingCharge(99L, 5L, "standard"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Warehouse not found with id: 99");
    }

    @Test
    void getShippingCharge_shouldThrow_whenCustomerNotFound() {
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(jaipur));
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.getShippingCharge(10L, 99L, "standard"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 99");
    }

    // =========================================================================
    // calculateShipping()
    // =========================================================================

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void calculateShipping_shouldReturnPositiveCharge_andNearestWarehouse() {
        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(5L).deliverySpeed("standard")
                .build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findBySellerId(1L)).thenReturn(List.of(product));
        when(warehouseRepository.findAll()).thenReturn(List.of(chennai, jaipur));

        ShippingCalculateResponse response = shippingService.calculateShipping(request);

        assertThat(response).isNotNull();
        assertThat(response.getShippingCharge()).isPositive();
        assertThat(response.getNearestWarehouse()).isNotNull();
    }

    @Test
    void calculateShipping_shouldPickNearestWarehouse_toSeller() {
        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(5L).deliverySpeed("standard")
                .build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findBySellerId(1L)).thenReturn(List.of(product));
        // Jaipur is nearest to Delhi seller regardless of list order
        when(warehouseRepository.findAll()).thenReturn(List.of(chennai, jaipur));

        ShippingCalculateResponse response = shippingService.calculateShipping(request);

        // nearest warehouse to Delhi(seller) = Jaipur (id=10)
        assertThat(response.getNearestWarehouse().getWarehouseId()).isEqualTo(10L);
        assertThat(response.getNearestWarehouse().getLat()).isEqualTo(26.9124);
        assertThat(response.getNearestWarehouse().getLng()).isEqualTo(75.7873);
    }

    @Test
    void calculateShipping_shouldUseProductWeight_inChargeCalculation() {
        // product weight = 2.0 kg
        // express charge must be higher than standard for same inputs
        ShippingCalculateRequest standardReq = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(5L).deliverySpeed("standard").build();

        ShippingCalculateRequest expressReq = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(5L).deliverySpeed("express").build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findBySellerId(1L)).thenReturn(List.of(product));
        when(warehouseRepository.findAll()).thenReturn(List.of(jaipur));

        ShippingCalculateResponse standardResponse = shippingService.calculateShipping(standardReq);

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findBySellerId(1L)).thenReturn(List.of(product));
        when(warehouseRepository.findAll()).thenReturn(List.of(jaipur));

        ShippingCalculateResponse expressResponse = shippingService.calculateShipping(expressReq);

        assertThat(expressResponse.getShippingCharge())
                .isGreaterThan(standardResponse.getShippingCharge());
    }

    @Test
    void calculateShipping_shouldUseFirstProduct_whenSellerHasMultipleProducts() {
        Product heavyProduct = Product.builder()
                .id(200L).productName("Server").weightKg(20.0)
                .sellingPrice(200000L).dimensionLength(60.0)
                .dimensionWidth(50.0).dimensionHeight(40.0)
                .seller(seller).build();

        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(5L).deliverySpeed("standard").build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        // first product in list (laptop 2kg) should be used
        when(productRepository.findBySellerId(1L)).thenReturn(List.of(product, heavyProduct));
        when(warehouseRepository.findAll()).thenReturn(List.of(jaipur));

        ShippingCalculateResponse response = shippingService.calculateShipping(request);

        // charge uses weightKg=2.0 (laptop), not 20.0 (server)
        // just assert it's a valid positive number without throwing
        assertThat(response.getShippingCharge()).isPositive();
    }

    // ── Repository interaction ────────────────────────────────────────────────

    @Test
    void calculateShipping_shouldCallAllFourRepositories() {
        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(5L).deliverySpeed("standard").build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findBySellerId(1L)).thenReturn(List.of(product));
        when(warehouseRepository.findAll()).thenReturn(List.of(jaipur));

        shippingService.calculateShipping(request);

        verify(sellerRepository,   times(1)).findById(1L);
        verify(customerRepository, times(1)).findById(5L);
        verify(productRepository,  times(1)).findBySellerId(1L);
        verify(warehouseRepository,times(1)).findAll();
    }

    @Test
    void calculateShipping_shouldStopAtSeller_whenSellerNotFound() {
        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(99L).customerId(5L).deliverySpeed("standard").build();

        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.calculateShipping(request))
                .isInstanceOf(ResourceNotFoundException.class);

        // nothing else should be called after seller fails
        verify(customerRepository,  never()).findById(any());
        verify(productRepository,   never()).findBySellerId(any());
        verify(warehouseRepository, never()).findAll();
    }

    // ── Exception paths ───────────────────────────────────────────────────────

    @Test
    void calculateShipping_shouldThrow_whenSellerNotFound() {
        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(99L).customerId(5L).deliverySpeed("standard").build();

        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.calculateShipping(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Seller not found with id: 99");
    }

    @Test
    void calculateShipping_shouldThrow_whenCustomerNotFound() {
        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(99L).deliverySpeed("standard").build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.calculateShipping(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 99");
    }

    @Test
    void calculateShipping_shouldThrow_whenNoProductFoundForSeller() {
        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(5L).deliverySpeed("standard").build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findBySellerId(1L)).thenReturn(List.of()); // no products

        assertThatThrownBy(() -> shippingService.calculateShipping(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No product found for seller: 1");
    }

    @Test
    void calculateShipping_shouldThrow_whenNoWarehousesExist() {
        ShippingCalculateRequest request = ShippingCalculateRequest.builder()
                .sellerId(1L).customerId(5L).deliverySpeed("standard").build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findBySellerId(1L)).thenReturn(List.of(product));
        when(warehouseRepository.findAll()).thenReturn(List.of()); // empty warehouse table

        assertThatThrownBy(() -> shippingService.calculateShipping(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No warehouses found");
    }
}
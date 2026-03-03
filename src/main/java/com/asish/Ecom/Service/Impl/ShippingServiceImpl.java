package com.asish.Ecom.Service.Impl;

import com.asish.Ecom.DTO.Request.ShippingCalculateRequest;
import com.asish.Ecom.DTO.Response.NearestWarehouseResponse;
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
import com.asish.Ecom.Service.ShippingService;
import com.asish.Ecom.Utils.DistanceCalculator;
import com.asish.Ecom.Utils.ShippingChargeCalculator;
import com.asish.Ecom.Utils.TransportModeSelector;
import com.asish.Ecom.Utils.WarehouseUtils;
import com.asish.Ecom.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;

    @Override
    @Cacheable(value = "shippingCharge", key = "#warehouseId + '-' + #customerId + '-' + #deliverySpeed")
    public ShippingChargeResponse getShippingCharge(Long warehouseId, Long customerId, String deliverySpeed) {

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        double distance = DistanceCalculator.calculateDistance(
                warehouse.getLatitude(), warehouse.getLongitude(),
                customer.getLatitude(), customer.getLongitude()
        );

        String transportMode = TransportModeSelector.selectMode(distance);
        double charge = ShippingChargeCalculator.calculate(distance, 1.0, transportMode, deliverySpeed);

        return ShippingChargeResponse.builder()
                .shippingCharge(charge)
                .build();
    }

    @Override
    public ShippingCalculateResponse calculateShipping(ShippingCalculateRequest request) {

        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + request.getSellerId()));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Product product = productRepository.findBySellerId(request.getSellerId())
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No product found for seller: " + request.getSellerId()));

        Warehouse nearestWarehouse = WarehouseUtils.findNearestWarehouse(
                warehouseRepository.findAll(),
                seller.getLatitude(),
                seller.getLongitude()
        );

        double distance = DistanceCalculator.calculateDistance(
                nearestWarehouse.getLatitude(), nearestWarehouse.getLongitude(),
                customer.getLatitude(), customer.getLongitude()
        );

        String transportMode = TransportModeSelector.selectMode(distance);
        double charge = ShippingChargeCalculator.calculate(
                distance, product.getWeightKg(), transportMode, request.getDeliverySpeed()
        );

        NearestWarehouseResponse warehouseResponse = NearestWarehouseResponse.builder()
                .warehouseId(nearestWarehouse.getId())
                .lat(nearestWarehouse.getLatitude())
                .lng(nearestWarehouse.getLongitude())
                .build();

        return ShippingCalculateResponse.builder()
                .shippingCharge(charge)
                .nearestWarehouse(warehouseResponse)
                .build();
    }
}
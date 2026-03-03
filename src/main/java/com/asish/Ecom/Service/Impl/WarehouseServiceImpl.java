package com.asish.Ecom.Service.Impl;

import com.asish.Ecom.DTO.Response.NearestWarehouseResponse;
import com.asish.Ecom.Entity.Seller;
import com.asish.Ecom.Entity.Warehouse;
import com.asish.Ecom.Repository.SellerRepository;
import com.asish.Ecom.Repository.WarehouseRepository;
import com.asish.Ecom.Service.WarehouseService;
import com.asish.Ecom.Utils.WarehouseUtils;
import com.asish.Ecom.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final SellerRepository sellerRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Cacheable(value = "nearestWarehouse", key = "#sellerId + '-' + #productId")
    public NearestWarehouseResponse getNearestWarehouse(Long sellerId, Long productId) {

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

        Warehouse nearestWarehouse = WarehouseUtils.findNearestWarehouse(
                warehouseRepository.findAll(),
                seller.getLatitude(),
                seller.getLongitude()
        );

        return NearestWarehouseResponse.builder()
                .warehouseId(nearestWarehouse.getId())
                .lat(nearestWarehouse.getLatitude())
                .lng(nearestWarehouse.getLongitude())
                .build();
    }
}

package com.asish.Ecom.Service;

import com.asish.Ecom.DTO.Response.NearestWarehouseResponse;

public interface WarehouseService {
    NearestWarehouseResponse getNearestWarehouse(Long sellerId, Long productId);
}
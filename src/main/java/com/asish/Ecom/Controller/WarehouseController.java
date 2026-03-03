package com.asish.Ecom.Controller;

import com.asish.Ecom.DTO.Response.NearestWarehouseResponse;
import com.asish.Ecom.Service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    // GET /api/v1/warehouse/nearest?sellerId=123&productId=456
    @GetMapping("/nearest")
    public ResponseEntity<NearestWarehouseResponse> getNearestWarehouse(
            @RequestParam Long sellerId,
            @RequestParam Long productId) {
        return ResponseEntity.ok(warehouseService.getNearestWarehouse(sellerId, productId));
    }
}
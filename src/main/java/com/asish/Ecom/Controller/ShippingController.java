package com.asish.Ecom.Controller;

import com.asish.Ecom.DTO.Request.ShippingCalculateRequest;
import com.asish.Ecom.DTO.Response.ShippingCalculateResponse;
import com.asish.Ecom.DTO.Response.ShippingChargeResponse;
import com.asish.Ecom.Service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipping-charge")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    // GET /api/v1/shipping-charge?warehouseId=789&customerId=456&deliverySpeed=standard
    @GetMapping
    public ResponseEntity<ShippingChargeResponse> getShippingCharge(
            @RequestParam Long warehouseId,
            @RequestParam Long customerId,
            @RequestParam String deliverySpeed) {
        return ResponseEntity.ok(shippingService.getShippingCharge(warehouseId, customerId, deliverySpeed));
    }

    // POST /api/v1/shipping-charge/calculate
    @PostMapping("/calculate")
    public ResponseEntity<ShippingCalculateResponse> calculateShipping(
            @RequestBody ShippingCalculateRequest request) {
        return ResponseEntity.ok(shippingService.calculateShipping(request));
    }
}

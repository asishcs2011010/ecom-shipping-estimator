package com.asish.Ecom.Service;

import com.asish.Ecom.DTO.Request.ShippingCalculateRequest;
import com.asish.Ecom.DTO.Response.ShippingCalculateResponse;
import com.asish.Ecom.DTO.Response.ShippingChargeResponse;

public interface ShippingService {
    ShippingChargeResponse getShippingCharge(Long warehouseId, Long customerId, String deliverySpeed);
    ShippingCalculateResponse calculateShipping(ShippingCalculateRequest request);
}
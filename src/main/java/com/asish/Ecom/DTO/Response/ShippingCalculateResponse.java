package com.asish.Ecom.DTO.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingCalculateResponse {
    private Double shippingCharge;
    private NearestWarehouseResponse nearestWarehouse;
}
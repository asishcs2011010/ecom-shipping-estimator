package com.asish.Ecom.DTO.Request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingCalculateRequest {
    private Long sellerId;
    private Long customerId;
    private String deliverySpeed;
}
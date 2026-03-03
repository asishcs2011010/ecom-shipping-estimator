package com.asish.Ecom.DTO.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearestWarehouseResponse {
    private Long warehouseId;
    private Double lat;
    private Double lng;
}
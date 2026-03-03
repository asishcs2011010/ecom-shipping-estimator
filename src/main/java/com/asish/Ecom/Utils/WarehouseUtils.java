package com.asish.Ecom.Utils;

import com.asish.Ecom.Entity.Warehouse;
import com.asish.Ecom.exception.ResourceNotFoundException;

import java.util.List;

public class WarehouseUtils {

    public static Warehouse findNearestWarehouse(List<Warehouse> warehouses, Double sellerLat, Double sellerLon) {
        return warehouses.stream()
                .min((w1, w2) -> {
                    double d1 = DistanceCalculator.calculateDistance(sellerLat, sellerLon, w1.getLatitude(), w1.getLongitude());
                    double d2 = DistanceCalculator.calculateDistance(sellerLat, sellerLon, w2.getLatitude(), w2.getLongitude());
                    return Double.compare(d1, d2);
                })
                .orElseThrow(() -> new ResourceNotFoundException("No warehouses found"));
    }
}
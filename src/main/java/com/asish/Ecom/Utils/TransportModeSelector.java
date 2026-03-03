package com.asish.Ecom.Utils;

import com.asish.Ecom.Constants.ShippingConstants;
import com.asish.Ecom.Constants.TransportMode;

public class TransportModeSelector {

    public static String selectMode(double distanceKm) {
        if (distanceKm >= ShippingConstants.AEROPLANE_MIN_DISTANCE) {
            return TransportMode.AEROPLANE;
        } else if (distanceKm >= ShippingConstants.TRUCK_MIN_DISTANCE) {
            return TransportMode.TRUCK;
        } else {
            return TransportMode.MINI_VAN;
        }
    }

    public static double getRatePerKmPerKg(String transportMode) {
        return switch (transportMode) {
            case TransportMode.AEROPLANE -> ShippingConstants.AEROPLANE_RATE;
            case TransportMode.TRUCK     -> ShippingConstants.TRUCK_RATE;
            case TransportMode.MINI_VAN  -> ShippingConstants.MINI_VAN_RATE;
            default -> throw new IllegalArgumentException("Unknown transport mode: " + transportMode);
        };
    }
}
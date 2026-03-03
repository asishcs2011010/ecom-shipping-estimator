package com.asish.Ecom.Utils;

import com.asish.Ecom.Constants.DeliverySpeed;
import com.asish.Ecom.Constants.ShippingConstants;

public class ShippingChargeCalculator {

    public static double calculate(double distanceKm, double weightKg,
                                   String transportMode, String deliverySpeed) {

        double transportRate = TransportModeSelector.getRatePerKmPerKg(transportMode);
        double shippingCharge = distanceKm * weightKg * transportRate;

        return switch (deliverySpeed.toLowerCase()) {
            case DeliverySpeed.STANDARD -> ShippingConstants.STANDARD_BASE_CHARGE + shippingCharge;
            case DeliverySpeed.EXPRESS  -> ShippingConstants.STANDARD_BASE_CHARGE + (ShippingConstants.EXPRESS_EXTRA_PER_KG * weightKg) + shippingCharge;
            default -> throw new IllegalArgumentException("Unknown delivery speed: " + deliverySpeed);
        };
    }
}
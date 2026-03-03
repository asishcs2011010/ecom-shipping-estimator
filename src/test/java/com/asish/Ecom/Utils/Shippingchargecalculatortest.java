package com.asish.Ecom.Utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShippingChargeCalculatorTest {

    /*
     * Constants (from ShippingConstants):
     *   STANDARD_BASE_CHARGE  = 10.0
     *   EXPRESS_EXTRA_PER_KG  =  1.2
     *
     * Constants (from ShippingConstants - rates):
     *   AEROPLANE_RATE = 1.0  /km/kg
     *   TRUCK_RATE     = 2.0  /km/kg
     *   MINI_VAN_RATE  = 3.0  /km/kg
     *
     * Formula:
     *   shippingCharge = distanceKm * weightKg * transportRate
     *   STANDARD → BASE(10) + shippingCharge
     *   EXPRESS  → BASE(10) + (1.2 * weightKg) + shippingCharge
     */

    // ─────────────────────────────────────────────────────────────────
    // STANDARD delivery
    // ─────────────────────────────────────────────────────────────────

    @Test
    void calculate_standard_miniVan_shouldReturnCorrectCharge() {
        // 10 + (100 * 2 * 3.0) = 10 + 600 = 610.0
        double result = ShippingChargeCalculator.calculate(100, 2, "MINI_VAN", "standard");
        assertEquals(610.0, result, 1e-9);
    }

    @Test
    void calculate_standard_truck_shouldReturnCorrectCharge() {
        // 10 + (200 * 5 * 2.0) = 10 + 2000 = 2010.0
        double result = ShippingChargeCalculator.calculate(200, 5, "TRUCK", "standard");
        assertEquals(2010.0, result, 1e-9);
    }

    @Test
    void calculate_standard_aeroplane_shouldReturnCorrectCharge() {
        // 10 + (1000 * 10 * 1.0) = 10 + 10000 = 10010.0
        double result = ShippingChargeCalculator.calculate(1000, 10, "AEROPLANE", "standard");
        assertEquals(10010.0, result, 1e-9);
    }

    @Test
    void calculate_standard_isCaseInsensitive() {
        // same as above but deliverySpeed = "STANDARD" (uppercase)
        double lower = ShippingChargeCalculator.calculate(100, 2, "MINI_VAN", "standard");
        double upper = ShippingChargeCalculator.calculate(100, 2, "MINI_VAN", "STANDARD");
        assertEquals(lower, upper, 1e-9);
    }

    // ─────────────────────────────────────────────────────────────────
    // EXPRESS delivery
    // ─────────────────────────────────────────────────────────────────

    @Test
    void calculate_express_miniVan_shouldIncludeExpressExtraPerKg() {
        // 10 + (1.2 * 2) + (100 * 2 * 3.0)
        // = 10 + 2.4 + 600 = 612.4
        double result = ShippingChargeCalculator.calculate(100, 2, "MINI_VAN", "express");
        assertEquals(612.4, result, 1e-9);
    }

    @Test
    void calculate_express_truck_shouldIncludeExpressExtraPerKg() {
        // 10 + (1.2 * 5) + (200 * 5 * 2.0)
        // = 10 + 6.0 + 2000 = 2016.0
        double result = ShippingChargeCalculator.calculate(200, 5, "TRUCK", "express");
        assertEquals(2016.0, result, 1e-9);
    }

    @Test
    void calculate_express_aeroplane_shouldIncludeExpressExtraPerKg() {
        // 10 + (1.2 * 10) + (1000 * 10 * 1.0)
        // = 10 + 12.0 + 10000 = 10022.0
        double result = ShippingChargeCalculator.calculate(1000, 10, "AEROPLANE", "express");
        assertEquals(10022.0, result, 1e-9);
    }

    @Test
    void calculate_express_isCaseInsensitive() {
        double lower = ShippingChargeCalculator.calculate(100, 2, "MINI_VAN", "express");
        double upper = ShippingChargeCalculator.calculate(100, 2, "MINI_VAN", "EXPRESS");
        assertEquals(lower, upper, 1e-9);
    }


    @Test
    void calculate_express_shouldAlwaysCostMoreThanStandard_forSameInputs() {
        double standard = ShippingChargeCalculator.calculate(300, 4, "TRUCK", "standard");
        double express  = ShippingChargeCalculator.calculate(300, 4, "TRUCK", "express");
        assertTrue(express > standard);
    }

    // ─────────────────────────────────────────────────────────────────
    // Edge cases
    // ─────────────────────────────────────────────────────────────────

    @Test
    void calculate_standard_zeroDistance_shouldReturnOnlyBaseCharge() {
        // 10 + (0 * 5 * 3.0) = 10.0
        double result = ShippingChargeCalculator.calculate(0, 5, "MINI_VAN", "standard");
        assertEquals(10.0, result, 1e-9);
    }

    @Test
    void calculate_express_zeroDistance_shouldReturnBaseChargeAndExpressExtra() {
        // 10 + (1.2 * 5) + (0 * 5 * 3.0) = 10 + 6.0 = 16.0
        double result = ShippingChargeCalculator.calculate(0, 5, "MINI_VAN", "express");
        assertEquals(16.0, result, 1e-9);
    }

    @Test
    void calculate_shouldThrowIllegalArgument_forUnknownDeliverySpeed() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ShippingChargeCalculator.calculate(100, 5, "TRUCK", "overnight")
        );
        assertTrue(ex.getMessage().contains("Unknown delivery speed: overnight"));
    }
}
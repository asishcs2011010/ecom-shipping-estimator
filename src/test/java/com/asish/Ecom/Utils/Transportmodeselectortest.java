package com.asish.Ecom.Utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransportModeSelectorTest {

    @Test
    void selectMode_shouldReturnAeroplane_whenDistanceExceedsAeroplaneMin() {
        assertEquals("AEROPLANE", TransportModeSelector.selectMode(1000.0));
    }

    @Test
    void selectMode_shouldReturnAeroplane_atExactAeroplaneThreshold() {

        assertEquals("AEROPLANE", TransportModeSelector.selectMode(500.0));
    }

    @Test
    void selectMode_shouldReturnTruck_whenDistanceBetweenTruckAndAeroplaneMin() {
        assertEquals("TRUCK", TransportModeSelector.selectMode(300.0));
    }

    @Test
    void selectMode_shouldReturnTruck_atExactTruckThreshold() {
        assertEquals("TRUCK", TransportModeSelector.selectMode(100.0));
    }

    @Test
    void selectMode_shouldReturnMiniVan_whenDistanceBelowTruckMin() {
        assertEquals("MINI_VAN", TransportModeSelector.selectMode(50.0));
    }

    @Test
    void selectMode_shouldReturnMiniVan_atZeroDistance() {
        assertEquals("MINI_VAN", TransportModeSelector.selectMode(0.0));
    }

    @Test
    void selectMode_shouldReturnMiniVan_justBelowTruckThreshold() {
        assertEquals("MINI_VAN", TransportModeSelector.selectMode(99.99));
    }

    @Test
    void selectMode_shouldReturnTruck_justBelowAeroplaneThreshold() {
        assertEquals("TRUCK", TransportModeSelector.selectMode(499.99));
    }


    @Test
    void getRatePerKmPerKg_shouldReturn1_forAeroplane() {
        assertEquals(1.0, TransportModeSelector.getRatePerKmPerKg("AEROPLANE"), 1e-9);
    }

    @Test
    void getRatePerKmPerKg_shouldReturn2_forTruck() {
        assertEquals(2.0, TransportModeSelector.getRatePerKmPerKg("TRUCK"), 1e-9);
    }

    @Test
    void getRatePerKmPerKg_shouldReturn3_forMiniVan() {
        assertEquals(3.0, TransportModeSelector.getRatePerKmPerKg("MINI_VAN"), 1e-9);
    }

    @Test
    void getRatePerKmPerKg_shouldThrowIllegalArgument_forUnknownMode() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TransportModeSelector.getRatePerKmPerKg("BICYCLE")
        );
        assertTrue(ex.getMessage().contains("Unknown transport mode: BICYCLE"));
    }
}
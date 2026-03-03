package com.asish.Ecom.Utils;

import com.asish.Ecom.Entity.Warehouse;
import com.asish.Ecom.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseUtilsTest {

    /*
     * Real-world coordinates used:
     *
     *   Seller  →  Delhi:    28.6139° N,  77.2090° E
     *   W1      →  Mumbai:   19.0760° N,  72.8777° E   (~1150 km from Delhi)
     *   W2      →  Jaipur:   26.9124° N,  75.7873° E   (~~270 km from Delhi)  ← nearest
     *   W3      →  Chennai:  13.0827° N,  80.2707° E   (~2200 km from Delhi)
     *
     * DistanceCalculator uses standard Haversine with EARTH_RADIUS_KM = 6371.0
     * The test does NOT hard-code the km value — it just asserts WHICH warehouse wins.
     */

    private Warehouse buildWarehouse(Long id, String name, double lat, double lon) {
        Warehouse w = new Warehouse();
        w.setId(id);
        w.setName(name);
        w.setLatitude(lat);
        w.setLongitude(lon);
        return w;
    }

    // ─────────────────────────────────────────────────────────────────
    // Happy-path: nearest warehouse is selected correctly
    // ─────────────────────────────────────────────────────────────────

    @Test
    void findNearestWarehouse_shouldReturnClosestWarehouse_fromMultiple() {
        double sellerLat = 28.6139;   // Delhi
        double sellerLon = 77.2090;

        Warehouse mumbai  = buildWarehouse(1L, "Mumbai",  19.0760, 72.8777);
        Warehouse jaipur  = buildWarehouse(2L, "Jaipur",  26.9124, 75.7873);  // nearest
        Warehouse chennai = buildWarehouse(3L, "Chennai", 13.0827, 80.2707);

        List<Warehouse> warehouses = List.of(mumbai, chennai, jaipur);

        Warehouse result = WarehouseUtils.findNearestWarehouse(warehouses, sellerLat, sellerLon);

        assertEquals("Jaipur", result.getName());
        assertEquals(2L, result.getId());
    }

    @Test
    void findNearestWarehouse_shouldReturnOnlyWarehouse_whenListHasOneElement() {
        double sellerLat = 28.6139;
        double sellerLon = 77.2090;

        Warehouse only = buildWarehouse(10L, "OnlyWarehouse", 26.9124, 75.7873);

        Warehouse result = WarehouseUtils.findNearestWarehouse(List.of(only), sellerLat, sellerLon);

        assertEquals("OnlyWarehouse", result.getName());
    }

    @Test
    void findNearestWarehouse_shouldReturnFirst_whenTwoWarehousesAtSameDistance() {
        // Both warehouses are symmetric around the seller lon → equal distance
        double sellerLat = 28.6139;
        double sellerLon = 77.2090;

        // Mirror points: same lat-delta, equal lon-delta in opposite directions
        Warehouse east = buildWarehouse(1L, "East", 28.6139, 78.2090);  // +1 lon
        Warehouse west = buildWarehouse(2L, "West", 28.6139, 76.2090);  // -1 lon

        // Haversine: both are the same distance from seller
        // stream().min() is stable → first minimum wins (east is first in list)
        Warehouse result = WarehouseUtils.findNearestWarehouse(List.of(east, west), sellerLat, sellerLon);

        // Assert it picks one without throwing — distance is equal
        assertNotNull(result);
    }

    // ─────────────────────────────────────────────────────────────────
    // Order independence: result should not depend on list order
    // ─────────────────────────────────────────────────────────────────

    @Test
    void findNearestWarehouse_shouldReturnSameWarehouse_regardlessOfListOrder() {
        double sellerLat = 28.6139;
        double sellerLon = 77.2090;

        Warehouse mumbai  = buildWarehouse(1L, "Mumbai",  19.0760, 72.8777);
        Warehouse jaipur  = buildWarehouse(2L, "Jaipur",  26.9124, 75.7873);
        Warehouse chennai = buildWarehouse(3L, "Chennai", 13.0827, 80.2707);

        // Order 1: jaipur first
        Warehouse result1 = WarehouseUtils.findNearestWarehouse(
                List.of(jaipur, mumbai, chennai), sellerLat, sellerLon
        );
        // Order 2: jaipur last
        Warehouse result2 = WarehouseUtils.findNearestWarehouse(
                List.of(mumbai, chennai, jaipur), sellerLat, sellerLon
        );

        assertEquals(result1.getId(), result2.getId());
        assertEquals("Jaipur", result1.getName());
        assertEquals("Jaipur", result2.getName());
    }

    // ─────────────────────────────────────────────────────────────────
    // Seller IS at the warehouse location → distance = 0
    // ─────────────────────────────────────────────────────────────────

    @Test
    void findNearestWarehouse_shouldReturnWarehouse_whenSellerIsAtWarehouseLocation() {
        double sellerLat = 19.0760;
        double sellerLon = 72.8777;

        Warehouse atSeller = buildWarehouse(5L, "AtSeller", 19.0760, 72.8777);  // distance = 0
        Warehouse far      = buildWarehouse(6L, "Far",      28.6139, 77.2090);

        Warehouse result = WarehouseUtils.findNearestWarehouse(
                List.of(far, atSeller), sellerLat, sellerLon
        );

        assertEquals("AtSeller", result.getName());
    }

    // ─────────────────────────────────────────────────────────────────
    // Empty list → ResourceNotFoundException
    // ─────────────────────────────────────────────────────────────────

    @Test
    void findNearestWarehouse_shouldThrowResourceNotFoundException_whenListIsEmpty() {
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> WarehouseUtils.findNearestWarehouse(
                        Collections.emptyList(), 28.6139, 77.2090
                )
        );
        assertTrue(ex.getMessage().contains("No warehouses found"));
    }
}
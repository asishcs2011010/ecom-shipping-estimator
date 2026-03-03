package com.asish.Ecom.Utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class DistanceCalculatorTest {

    private static final double DELTA = 1.0;

    @Test
    void shouldReturnZero_whenSameCoordinates() {
        double distance = DistanceCalculator.calculateDistance(
                28.6139, 77.2090,
                28.6139, 77.2090
        );
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void shouldReturnKnownDistance_betweenDelhiAndMumbai() {
        double distance = DistanceCalculator.calculateDistance(
                28.6139, 77.2090,
                19.0760, 72.8777
        );
        assertThat(distance).isCloseTo(1148.09, offset(DELTA));
    }

    @Test
    void shouldBeSymmetric_AtoB_equalsBtoA() {
        double aToB = DistanceCalculator.calculateDistance(28.6139, 77.2090, 19.0760, 72.8777);
        double bToA = DistanceCalculator.calculateDistance(19.0760, 72.8777, 28.6139, 77.2090);
        assertThat(aToB).isCloseTo(bToA, offset(1e-9));
    }

    @Test
    void shouldHandleNegativeCoordinates_southernHemisphere() {
        double distance = DistanceCalculator.calculateDistance(
                -33.8688, 151.2093,
                -37.8136, 144.9631
        );
        assertThat(distance).isCloseTo(713.6, offset(DELTA));
    }
}
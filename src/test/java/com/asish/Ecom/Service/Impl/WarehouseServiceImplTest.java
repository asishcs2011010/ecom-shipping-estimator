package com.asish.Ecom.Service.Impl;

import com.asish.Ecom.DTO.Response.NearestWarehouseResponse;
import com.asish.Ecom.Entity.Seller;
import com.asish.Ecom.Entity.Warehouse;
import com.asish.Ecom.Repository.SellerRepository;
import com.asish.Ecom.Repository.WarehouseRepository;
import com.asish.Ecom.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    // ─── Test data ────────────────────────────────────────────────────────────

    private Seller seller;
    private Warehouse nearWarehouse;
    private Warehouse farWarehouse;

    @BeforeEach
    void setUp() {
        // Seller in Delhi
        seller = Seller.builder()
                .id(1L)
                .name("Delhi Seller")
                .latitude(28.6139)
                .longitude(77.2090)
                .build();

        // Jaipur ~270 km from Delhi  ← should be picked as nearest
        nearWarehouse = Warehouse.builder()
                .id(10L)
                .name("Jaipur Warehouse")
                .latitude(26.9124)
                .longitude(75.7873)
                .build();

        // Chennai ~2200 km from Delhi
        farWarehouse = Warehouse.builder()
                .id(20L)
                .name("Chennai Warehouse")
                .latitude(13.0827)
                .longitude(80.2707)
                .build();
    }

    // ─── Happy path ───────────────────────────────────────────────────────────

    @Test
    void getNearestWarehouse_shouldReturnNearestWarehouse_whenSellerAndWarehousesExist() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(warehouseRepository.findAll()).thenReturn(List.of(farWarehouse, nearWarehouse));

        NearestWarehouseResponse response = warehouseService.getNearestWarehouse(1L, 100L);

        assertThat(response).isNotNull();
        assertThat(response.getWarehouseId()).isEqualTo(10L);               // Jaipur wins
        assertThat(response.getLat()).isEqualTo(26.9124);
        assertThat(response.getLng()).isEqualTo(75.7873);
    }

    @Test
    void getNearestWarehouse_shouldReturnCorrectWarehouseId_fromResponseDTO() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(warehouseRepository.findAll()).thenReturn(List.of(nearWarehouse));

        NearestWarehouseResponse response = warehouseService.getNearestWarehouse(1L, 999L);

        assertThat(response.getWarehouseId()).isEqualTo(nearWarehouse.getId());
        assertThat(response.getLat()).isEqualTo(nearWarehouse.getLatitude());
        assertThat(response.getLng()).isEqualTo(nearWarehouse.getLongitude());
    }

    @Test
    void getNearestWarehouse_shouldPickNearest_whenMultipleWarehousesExist() {
        Warehouse mumbai = Warehouse.builder()
                .id(30L).name("Mumbai").latitude(19.0760).longitude(72.8777).build();

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(warehouseRepository.findAll()).thenReturn(List.of(farWarehouse, mumbai, nearWarehouse));

        NearestWarehouseResponse response = warehouseService.getNearestWarehouse(1L, 1L);

        // Jaipur is closest to Delhi among all three
        assertThat(response.getWarehouseId()).isEqualTo(10L);
    }

    // ─── Repository interaction ───────────────────────────────────────────────

    @Test
    void getNearestWarehouse_shouldCallSellerRepository_withCorrectId() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(warehouseRepository.findAll()).thenReturn(List.of(nearWarehouse));

        warehouseService.getNearestWarehouse(1L, 100L);

        verify(sellerRepository, times(1)).findById(1L);
    }

    @Test
    void getNearestWarehouse_shouldCallWarehouseRepository_findAll() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(warehouseRepository.findAll()).thenReturn(List.of(nearWarehouse));

        warehouseService.getNearestWarehouse(1L, 100L);

        verify(warehouseRepository, times(1)).findAll();
    }

    @Test
    void getNearestWarehouse_shouldNeverCallWarehouseRepo_whenSellerNotFound() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getNearestWarehouse(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        // warehouseRepository must never be touched if seller lookup fails
        verify(warehouseRepository, never()).findAll();
    }

    // ─── Exception paths ──────────────────────────────────────────────────────

    @Test
    void getNearestWarehouse_shouldThrowResourceNotFoundException_whenSellerNotFound() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getNearestWarehouse(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Seller not found with id: 99");
    }

    @Test
    void getNearestWarehouse_shouldThrowResourceNotFoundException_whenNoWarehousesExist() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(warehouseRepository.findAll()).thenReturn(List.of());   // empty warehouse table

        assertThatThrownBy(() -> warehouseService.getNearestWarehouse(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No warehouses found");
    }

    @Test
    void getNearestWarehouse_shouldThrowResourceNotFoundException_withExactMessage_whenSellerMissing() {
        Long missingId = 42L;
        when(sellerRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getNearestWarehouse(missingId, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Seller not found with id: 42");
    }
}
package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.packaging.request.PackagingRequest;
import com.nhnacademy.order_payments.dto.packaging.response.PackagingResponse;
import com.nhnacademy.order_payments.entity.Packaging;
import com.nhnacademy.order_payments.repository.PackagingRepository;
import com.nhnacademy.order_payments.service.packaging.impl.PackagingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 포장 정책 서비스 로직 테스트
// 생성/조회/수정/삭제가 잘 동작하는지 확인
@ExtendWith(MockitoExtension.class)
public class PackagingServiceImplTest {

    @Mock
    PackagingRepository packagingRepository;

    PackagingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PackagingServiceImpl(packagingRepository);
    }

    @Test
    @DisplayName("getPackagingName - packagingId가 null이면 null")
    void getPackagingName_nullId() {
        String name = service.getPackagingName(null);
        assertNull(name);
        verifyNoInteractions(packagingRepository);
    }

    @Test
    @DisplayName("getPackagingName - 존재하면 이름 반환, 없으면 null")
    void getPackagingName_found_or_null() {
        when(packagingRepository.findById(1L))
                .thenReturn(Optional.of(new Packaging("포장지A", 1000L, true)));

        assertEquals("포장지A", service.getPackagingName(1L));

        when(packagingRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertNull(service.getPackagingName(2L));
    }

    @Test
    @DisplayName("createPackaging - 저장 호출 및 값 매핑")
    void createPackaging() {
        PackagingRequest req = new PackagingRequest("포장지A", 1000L, true);

        service.createPackaging(req);

        ArgumentCaptor<Packaging> captor = ArgumentCaptor.forClass(Packaging.class);
        verify(packagingRepository).save(captor.capture());

        Packaging saved = captor.getValue();
        assertEquals("포장지A", saved.getPackagingName());
        assertEquals(1000L, saved.getPrice());
        assertTrue(saved.getEnabled());
    }

    @Test
    @DisplayName("getPackagings - 전체 조회 매핑")
    void getPackagings() {
        when(packagingRepository.findAll()).thenReturn(List.of(
                new Packaging("A", 1000L, true),
                new Packaging("B", 2000L, false)
        ));

        List<PackagingResponse> res = service.getPackagings();
        assertEquals(2, res.size());
        assertEquals("A", res.get(0).packagingName());
        assertEquals(2000L, res.get(1).price());
    }

    @Test
    @DisplayName("getEnabledPackagings - enabled=true만 매핑")
    void getEnabledPackagings() {
        when(packagingRepository.findAllByEnabled(true)).thenReturn(List.of(
                new Packaging("A", 1000L, true)
        ));

        List<PackagingResponse> res = service.getEnabledPackagings();
        assertEquals(1, res.size());
        assertTrue(res.getFirst().enabled());
    }

    @Test
    @DisplayName("modifyPackaging - 없으면 예외, 있으면 엔티티 값 변경")
    void modifyPackaging() {
        when(packagingRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.modifyPackaging(10L, new PackagingRequest("X", 1L, true)));

        Packaging packaging = new Packaging("기존", 1000L, true);
        when(packagingRepository.findById(10L)).thenReturn(Optional.of(packaging));

        service.modifyPackaging(10L, new PackagingRequest("수정", 2000L, false));

        assertEquals("수정", packaging.getPackagingName());
        assertEquals(2000L, packaging.getPrice());
        assertFalse(packaging.getEnabled());
    }

    @Test
    @DisplayName("deletePackaging - 없으면 예외, 있으면 deleteById")
    void deletePackaging() {
        when(packagingRepository.existsById(10L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.deletePackaging(10L));

        when(packagingRepository.existsById(10L)).thenReturn(true);
        service.deletePackaging(10L);
        verify(packagingRepository).deleteById(10L);
    }
}

package com.nhnacademy.order_payments.service.cart;

import com.nhnacademy.order_payments.dto.cart.SyncDto;
import com.nhnacademy.order_payments.dto.cart.SyncInfo;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.repository.CartDetailRepository;
import com.nhnacademy.order_payments.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SyncServiceTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    CartDetailRepository cartDetailRepository;

    SyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new SyncService(cartRepository, cartDetailRepository);
    }

    @Test
    @DisplayName("기존 항목 수량이 바뀌면 엔티티 수량을 갱신해서 저장한다 (Integer 캐시 범위 밖 값 포함)")
    void syncDB_updatesQuantityOfExistingDetail() {
        Cart cart = cartWithId(10L, 1L);
        CartDetail existing = new CartDetail(cart, 100L, 200); // 200: Integer 캐시(-128~127) 밖
        when(cartRepository.findFirstByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartDetailRepository.findAllByCartCartIdAndBookIdIn(eq(1L), anyList()))
                .thenReturn(List.of(existing));

        syncService.syncDB(new SyncDto(List.of(new SyncInfo(10L, 100L, 300))));

        ArgumentCaptor<List<CartDetail>> captor = ArgumentCaptor.forClass(List.class);
        verify(cartDetailRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(300, captor.getValue().get(0).getQuantity());
        assertSame(existing, captor.getValue().get(0));
    }

    @Test
    @DisplayName("수량이 같으면 (Integer 캐시 범위 밖이어도) 변경 없음으로 판단해 저장하지 않는다")
    void syncDB_skipsUnchangedQuantityOutsideIntegerCache() {
        Cart cart = cartWithId(10L, 1L);
        CartDetail existing = new CartDetail(cart, 100L, 200);
        when(cartRepository.findFirstByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartDetailRepository.findAllByCartCartIdAndBookIdIn(eq(1L), anyList()))
                .thenReturn(List.of(existing));

        syncService.syncDB(new SyncDto(List.of(new SyncInfo(10L, 100L, 200))));

        verify(cartDetailRepository, never()).saveAll(any());
        assertEquals(200, existing.getQuantity());
    }

    @Test
    @DisplayName("DB에 없는 항목은 새 CartDetail로 insert된다")
    void syncDB_insertsNewDetail() {
        Cart cart = cartWithId(10L, 1L);
        when(cartRepository.findFirstByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartDetailRepository.findAllByCartCartIdAndBookIdIn(eq(1L), anyList()))
                .thenReturn(List.of());

        syncService.syncDB(new SyncDto(List.of(new SyncInfo(10L, 100L, 2))));

        ArgumentCaptor<List<CartDetail>> captor = ArgumentCaptor.forClass(List.class);
        verify(cartDetailRepository).saveAll(captor.capture());
        CartDetail saved = captor.getValue().get(0);
        assertEquals(100L, saved.getBookId());
        assertEquals(2, saved.getQuantity());
        assertSame(cart, saved.getCart());
    }

    @Test
    @DisplayName("사용자별 저장 목록이 분리되어 이전 사용자의 항목이 다음 사용자 saveAll에 섞이지 않는다")
    void syncDB_saveListIsScopedPerUser() {
        Cart cartA = cartWithId(10L, 1L);
        Cart cartB = cartWithId(20L, 2L);
        when(cartRepository.findFirstByUserId(10L)).thenReturn(Optional.of(cartA));
        when(cartRepository.findFirstByUserId(20L)).thenReturn(Optional.of(cartB));
        when(cartDetailRepository.findAllByCartCartIdAndBookIdIn(anyLong(), anyList()))
                .thenReturn(List.of());

        syncService.syncDB(new SyncDto(List.of(
                new SyncInfo(10L, 100L, 1),
                new SyncInfo(20L, 200L, 1)
        )));

        ArgumentCaptor<List<CartDetail>> captor = ArgumentCaptor.forClass(List.class);
        verify(cartDetailRepository, times(2)).saveAll(captor.capture());
        for (List<CartDetail> batch : captor.getAllValues()) {
            assertEquals(1, batch.size(), "각 saveAll 호출은 해당 사용자의 항목 1건만 포함해야 함");
        }
    }

    @Test
    @DisplayName("quantity 0은 삭제, bookId -1은 전체 비우기로 처리한다")
    void syncDB_handlesDeleteAndClearFlags() {
        Cart cart = cartWithId(10L, 1L);
        when(cartRepository.findFirstByUserId(anyLong())).thenReturn(Optional.of(cart));

        syncService.syncDB(new SyncDto(List.of(new SyncInfo(10L, 100L, 0))));
        verify(cartDetailRepository).removeByBookIdInAndCartUserId(List.of(100L), 10L);

        syncService.syncDB(new SyncDto(List.of(new SyncInfo(10L, -1L, -1))));
        verify(cartDetailRepository).removeByCartUserId(10L);
        verify(cartDetailRepository, never()).saveAll(any());
    }

    private Cart cartWithId(Long userId, Long cartId) {
        Cart cart = new Cart(userId);
        try {
            var f = Cart.class.getDeclaredField("cartId");
            f.setAccessible(true);
            f.set(cart, cartId);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return cart;
    }
}

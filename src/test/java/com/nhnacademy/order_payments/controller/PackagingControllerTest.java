package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.packaging.response.PackagingResponse;
import com.nhnacademy.order_payments.service.packaging.PackagingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PackagingController.class)
@AutoConfigureMockMvc(addFilters = false)
class PackagingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PackagingService packagingService;

    @MockitoBean(name = "jpaMappingContext")
    JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("POST /api/admin/packagings - 포장 정책 등록(201)")
    void createPackaging() throws Exception {
        String json = """
                {
                  "packagingName": "포장지A",
                  "price": 1000,
                  "enabled": true
                }
                """;

        mockMvc.perform(post("/api/admin/packagings")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(packagingService).createPackaging(any());
    }

    @Test
    @DisplayName("GET /api/admin/packagings - 전체 조회(200)")
    void getPackagings() throws Exception {
        given(packagingService.getPackagings()).willReturn(List.of(
                new PackagingResponse(1L, "포장지A", 1000L, true)
        ));

        mockMvc.perform(get("/api/admin/packagings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].packagingName").value("포장지A"))
                .andExpect(jsonPath("$[0].price").value(1000))
                .andExpect(jsonPath("$[0].enabled").value(true));

        verify(packagingService).getPackagings();
    }

    @Test
    @DisplayName("PUT /api/admin/packagings/{id} - 수정(200)")
    void modifyPackaging() throws Exception {
        String json = """
                {
                  "packagingName": "포장지B",
                  "price": 2000,
                  "enabled": false
                }
                """;

        mockMvc.perform(put("/api/admin/packagings/{id}", 10L)
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(packagingService).modifyPackaging(eq(10L), any());
    }

    @Test
    @DisplayName("DELETE /api/admin/packagings/{id} - 삭제(204)")
    void deletePackaging() throws Exception {
        mockMvc.perform(delete("/api/admin/packagings/{id}", 10L)
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());

        verify(packagingService).deletePackaging(10L);
    }
}

package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.response.DeliveryPolicyResponse;
import com.nhnacademy.order_payments.service.delivery.DeliveryPolicyService;
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

// 배송 정책 컨트롤러 연결 테스트
// deliveryPolicyService를 mock으로 두고 테스트
@WebMvcTest(DeliveryPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DeliveryPolicyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DeliveryPolicyService deliveryPolicyService;

    @MockitoBean(name = "jpaMappingContext")
    JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("POST /api/admin/deliveries/policies - 정책 등록(201)")
    void createPolicy() throws Exception {
        String json = """
                {
                  "policyName": "기본 정책",
                  "deliveryFee": 5000,
                  "freeMinimumAmount": 30000
                }
                """;

        mockMvc.perform(post("/api/admin/deliveries/policies")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(deliveryPolicyService).createPolicy(any());
    }

    @Test
    @DisplayName("GET /api/admin/deliveries/policies - 정책 전체 조회(200)")
    void getPolicies() throws Exception {
        given(deliveryPolicyService.getPolicies()).willReturn(List.of(
                new DeliveryPolicyResponse(1L, "기본 정책", 5000L, 30000L)
        ));

        mockMvc.perform(get("/api/admin/deliveries/policies")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyName").value("기본 정책"))
                .andExpect(jsonPath("$[0].deliveryFee").value(5000));

        verify(deliveryPolicyService).getPolicies();
    }

    @Test
    @DisplayName("PUT /api/admin/deliveries/policies/{id} - 정책 수정(200)")
    void modifyPolicy() throws Exception {
        String json = """
                {
                  "policyName": "수정 정책",
                  "deliveryFee": 6000,
                  "freeMinimumAmount": 40000
                }
                """;

        mockMvc.perform(put("/api/admin/deliveries/policies/{id}", 10L)
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(deliveryPolicyService).modifyPolicy(eq(10L), any());
    }

    @Test
    @DisplayName("DELETE /api/admin/deliveries/policies/{id} - 정책 삭제(204)")
    void deletePolicy() throws Exception {
        mockMvc.perform(delete("/api/admin/deliveries/policies/{id}", 10L)
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());

        verify(deliveryPolicyService).deletePolicy(10L);
    }
}

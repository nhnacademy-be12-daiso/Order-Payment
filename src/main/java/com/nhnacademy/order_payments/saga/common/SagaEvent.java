package com.nhnacademy.order_payments.saga.common;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhnacademy.order_payments.saga.SagaOrchestrator;
import com.nhnacademy.order_payments.saga.event.OrderCompensateEvent;
import com.nhnacademy.order_payments.saga.event.OrderConfirmedEvent;
import com.nhnacademy.order_payments.saga.event.OrderRefundEvent;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // JSON에 "type": "CONFIRMED" 이런 식으로 정보가 붙어
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderConfirmedEvent.class, name = "CONFIRMED"),
        @JsonSubTypes.Type(value = OrderRefundEvent.class, name = "REFUND"),
        @JsonSubTypes.Type(value = OrderCompensateEvent.class, name = "COMPENSATE")
})
public interface SagaEvent {

    String getEventId();
    Long getOrderId();

}

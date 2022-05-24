package com.lsoftware.estore.core.events;

import com.lsoftware.estore.core.model.OrderStatus;

import lombok.Value;

@Value
public class OrderRejectedEvent {
	
	private final String orderId;
	private final String reason;
	private final OrderStatus orderStatus = OrderStatus.REJECTED;

}

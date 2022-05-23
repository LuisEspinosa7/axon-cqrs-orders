package com.lsoftware.estore.core.events;

import com.lsoftware.estore.core.model.OrderStatus;

import lombok.Value;

@Value // Gets everything for a POJO
public class OrderApprovedEvent {
	
	private final String orderId;
	private final OrderStatus orderStatus = OrderStatus.APPROVED;

}

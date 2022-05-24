package com.lsoftware.estore.query;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.lsoftware.estore.core.data.OrderEntity;
import com.lsoftware.estore.core.data.OrdersRepository;
import com.lsoftware.estore.core.model.OrderSummary;

@Component
public class OrderQueriesHandler {
	
	OrdersRepository ordersRepository;
	
	public OrderQueriesHandler(OrdersRepository ordersRepository) {
		this.ordersRepository = ordersRepository;
	}
	
	@QueryHandler
	public OrderSummary findOrder(FindOrderQuery findOrderQuery) {
		OrderEntity orderEntity = ordersRepository.findByOrderId(findOrderQuery.getOrderId());
		return new OrderSummary(orderEntity.getOrderId(), orderEntity.getOrderStatus(), "");
	}

}

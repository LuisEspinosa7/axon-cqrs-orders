package com.lsoftware.estore.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.lsoftware.estore.core.data.OrderEntity;
import com.lsoftware.estore.core.data.OrdersRepository;
import com.lsoftware.estore.core.events.OrderApprovedEvent;
import com.lsoftware.estore.core.events.OrderCreatedEvent;

@Component
@ProcessingGroup("order-group")
public class OrderEventsHandler {
    
    private final OrdersRepository ordersRepository;
    
    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) throws Exception {
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(event, orderEntity);
 
        this.ordersRepository.save(orderEntity);
    }
    
    
    @EventHandler
    public void on(OrderApprovedEvent event) throws Exception {
        OrderEntity orderEntity = ordersRepository.findByOrderId(event.getOrderId());
        
        if (orderEntity == null) {
			// Do something
        	return;
		}
        orderEntity.setOrderStatus(event.getOrderStatus());
        this.ordersRepository.save(orderEntity);
    }
    
}

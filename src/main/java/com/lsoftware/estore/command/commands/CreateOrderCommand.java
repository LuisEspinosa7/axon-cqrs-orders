package com.lsoftware.estore.command.commands;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.lsoftware.estore.core.model.OrderStatus;

@Builder
@Data
public class CreateOrderCommand {
        
    @TargetAggregateIdentifier
    public final String orderId;
    
    private final String userId;
    private final String productId;
    private final int quantity;
    private final String addressId; 
    private final OrderStatus orderStatus;
}
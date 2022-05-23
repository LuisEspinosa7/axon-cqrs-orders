package com.lsoftware.estore.saga;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lsoftware.estore.core.events.OrderCreatedEvent;
import com.lsoftware.estore.shared.core.commands.ReserveProductCommand;
import com.lsoftware.estore.shared.core.events.ProductReservedEvent;
import com.lsoftware.estore.shared.core.model.User;
import com.lsoftware.estore.shared.core.query.FetchUserPaymentDetailsQuery;

@Saga
public class OrderSaga {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);
	
	@Autowired
	private transient CommandGateway commandGateway;
	
	@Autowired
	private transient QueryGateway queryGateway;
	
	@StartSaga
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(OrderCreatedEvent orderCreatedEvent) {
		ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
				.orderId(orderCreatedEvent.getOrderId())
				.productId(orderCreatedEvent.getProductId())
				.quantity(orderCreatedEvent.getQuantity())
				.userId(orderCreatedEvent.getUserId())
				.build();
		
		LOGGER.info("OrderCreatedEvent handled for orderId: " + reserveProductCommand.getOrderId() + 
				" and productId" + reserveProductCommand.getProductId());
		
		commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {

			@Override
			public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
					CommandResultMessage<? extends Object> commandResultMessage) {
				
				if (commandResultMessage.isExceptional()) {
					// Start a compensating transaction
					
				}
				
			}
			
		});
	}
	
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(ProductReservedEvent productReservedEvent) {
		// Process user payment....
		LOGGER.info("ProductReservedEvent handled for productId: " + productReservedEvent.getProductId() + 
				" and orderId" + productReservedEvent.getOrderId());
		LOGGER.info("Processing payment....");
		
		FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery = 
				new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());
		
		User userPaymentDetails = null;
		
		try {
			userPaymentDetails = queryGateway
					.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class))
					.join();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			// Start a compensating transaction
			return;
		}
		
		if (userPaymentDetails == null) {
			// Start a compensating transaction
			return;
		}
		
		LOGGER.info("Successfully fetched user payment details for user: " + userPaymentDetails.getFirstName()); 
	}

}

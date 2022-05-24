package com.lsoftware.estore.saga;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lsoftware.estore.command.commands.ApproveOrderCommand;
import com.lsoftware.estore.command.commands.RejectOrderCommand;
import com.lsoftware.estore.core.events.OrderApprovedEvent;
import com.lsoftware.estore.core.events.OrderCreatedEvent;
import com.lsoftware.estore.core.events.OrderRejectedEvent;
import com.lsoftware.estore.shared.core.commands.CancelProductReservationCommand;
import com.lsoftware.estore.shared.core.commands.ProcessPaymentCommand;
import com.lsoftware.estore.shared.core.commands.ReserveProductCommand;
import com.lsoftware.estore.shared.core.events.PaymentProcessedEvent;
import com.lsoftware.estore.shared.core.events.ProductReservationCancelledEvent;
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
			cancelProductReservation(productReservedEvent, e.getMessage());
			return;
		}
		
		if (userPaymentDetails == null) {
			// Start a compensating transaction
			cancelProductReservation(productReservedEvent, "Could not fetch user payment details");
			return;
		}
		
		LOGGER.info("Successfully fetched user payment details for user: " + userPaymentDetails.getFirstName()); 
	
		ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand
				.builder()
				.orderId(productReservedEvent.getOrderId())
				.paymentDetails(userPaymentDetails.getPaymentDetails())
				.paymentId(UUID.randomUUID().toString())
				.build();
		
		String result = null;
		boolean compensated = false;
		try {
			result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			// Start a compensating transaction
			compensated = true;
			cancelProductReservation(productReservedEvent, e.getMessage());
		}
		
		if (!compensated && result == null) {
			LOGGER.info("The ProcessPaymentCommand resulted in NULL. Initiating a compansation transaction");
			// Start a compensating transaction
			cancelProductReservation(productReservedEvent, "Could not proccess user payment with provided payment");
		}
		
	}
	
	
	private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {
		CancelProductReservationCommand cancelProductReservationCommand = 
				CancelProductReservationCommand.builder()
				.orderId(productReservedEvent.getOrderId())
				.productId(productReservedEvent.getProductId())
				.quantity(productReservedEvent.getQuantity())
				.userId(productReservedEvent.getUserId())
				.reason(reason)
				.build();
	
		commandGateway.send(cancelProductReservationCommand);
	}
	
	
	
	
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(PaymentProcessedEvent paymentProcessedEvent) {
		// Publish a ApproveOrderCommand
		ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());
		commandGateway.send(approveOrderCommand);
	}
	
	@EndSaga
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(OrderApprovedEvent orderApprovedEvent) {
		LOGGER.info("Order is approved. Order saga is completed for orderId: " + orderApprovedEvent);
		//SagaLifecycle.end();
	}
	
	
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {
		// Create and send a RejectOrderCommand
		RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCancelledEvent.getOrderId(),
				productReservationCancelledEvent.getReason());
		commandGateway.send(rejectOrderCommand);
	}
	
	//@EndSaga
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(OrderRejectedEvent orderRejectedEvent) {
		LOGGER.info("Successfully rejected order with id: " + orderRejectedEvent.getOrderId());
		//SagaLifecycle.end();
	}

}

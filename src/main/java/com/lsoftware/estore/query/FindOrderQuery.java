package com.lsoftware.estore.query;

import lombok.Value;

@Value
public class FindOrderQuery {
	private final String orderId;
}

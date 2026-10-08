package com.duck.warehouse.store.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * totalToPay = goodsCost + every line adjustment + every order adjustment =
 * sum(lines[].total) + sum(adjustments[].amount)
 */
public record OrderResponse(String destination, String shippingMode, String currency, int totalUnits,
		List<OrderLineResponse> lines, BigDecimal goodsCost, List<AdjustmentResponse> adjustments,
		BigDecimal totalToPay) {

	/**
	 * One ordered item: its own package, protection and package cost. {@code total}
	 * excludes order-level charges.
	 */
	public record OrderLineResponse(String color, String size, int quantity, BigDecimal unitPrice, String packageType,
			List<String> protections, BigDecimal goodsCost, List<AdjustmentResponse> adjustments, BigDecimal total) {
	}

	public record AdjustmentResponse(String name, BigDecimal amount) {
	}
}

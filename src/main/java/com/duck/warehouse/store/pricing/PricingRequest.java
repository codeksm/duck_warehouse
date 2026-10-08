package com.duck.warehouse.store.pricing;

import java.util.List;

import com.duck.warehouse.store.domain.Destination;
import com.duck.warehouse.store.domain.ShippingMode;

/** A whole order: one destination, one shipping mode, one or more lines. */
public record PricingRequest(Destination destination, ShippingMode shippingMode, List<PricingLine> lines) {

	public PricingRequest {
		lines = List.copyOf(lines);
	}

	/**
	 * Sum of units over all lines: the "units" every order-level threshold is
	 * measured in.
	 */
	public int totalUnits() {
		return lines.stream().mapToInt(PricingLine::quantity).sum();
	}
}

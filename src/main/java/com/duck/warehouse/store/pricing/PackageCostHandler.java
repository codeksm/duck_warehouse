package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;

/**
 * Stage 3 - rules 3-5, per LINE (each line has its own package): wood +5%,
 * plastic +10%, cardboard -1%, applied to that line's cost after the bulk
 * discount.
 */
final class PackageCostHandler extends PricingHandler {
	@Override
	protected void apply(PricingState state) {
		for (PricingState.LineState line : state.lines()) {
			var packageType = line.input().packageType();
			// Switch expression over an enum: the compiler forces us to handle any package
			// type added later.
			BigDecimal rate = switch (packageType) {
			case WOOD -> new BigDecimal("0.05");
			case PLASTIC -> new BigDecimal("0.10");
			case CARDBOARD -> new BigDecimal("-0.01");
			};
			line.add(PriceAdjustment.ofRate(packageType.label() + " package", state.lineBase(line), rate));
		}
	}
}

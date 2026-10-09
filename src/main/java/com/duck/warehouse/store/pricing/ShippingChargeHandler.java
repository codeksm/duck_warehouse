package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stage 5 (last link, the stop step) - rules 10-12, once per ORDER, measured in
 * the order's total units. Shipping charges are flat or per unit, so they do
 * not use the percentage base.
 */
final class ShippingChargeHandler extends PricingHandler {
	private static final BigDecimal SEA_FLAT_FEE = new BigDecimal("400");
	private static final BigDecimal LAND_PER_UNIT = new BigDecimal("10");
	private static final BigDecimal AIR_PER_UNIT = new BigDecimal("30");
	private static final int AIR_BULK_THRESHOLD = 1000;
	private static final BigDecimal AIR_BULK_REDUCTION = new BigDecimal("-0.15");

	@Override
	protected void apply(PricingState state) {
		int units = state.totalUnits();
		List<PriceAdjustment> charges = switch (state.request().shippingMode()) {
		case SEA -> sea();
		case LAND -> land(units);
		case AIR -> air(units);
		};
		charges.forEach(state::addOrderAdjustment);
	}

	private List<PriceAdjustment> sea() {
		return List.of(PriceAdjustment.flat("Sea shipping flat fee", SEA_FLAT_FEE));
	}

	private List<PriceAdjustment> land(int units) {
		return List.of(PriceAdjustment.flat("Land shipping (10.00 USD x " + units + " units)",
				LAND_PER_UNIT.multiply(BigDecimal.valueOf(units))));
	}

	private List<PriceAdjustment> air(int units) {
		PriceAdjustment charge = PriceAdjustment.flat("Air shipping (30.00 USD x " + units + " units)",
				AIR_PER_UNIT.multiply(BigDecimal.valueOf(units)));
		if (units <= AIR_BULK_THRESHOLD) {
			return List.of(charge);
		}
		// The 15% reduction applies to the air charge only.
		return List.of(charge, PriceAdjustment.ofRate("Air shipping bulk (>" + AIR_BULK_THRESHOLD + " units)",
				charge.amount(), AIR_BULK_REDUCTION));
	}
}

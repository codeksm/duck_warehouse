package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;

/**
 * Stage 2 - rule 2: more than 100 units IN THE ORDER -> 20% discount on the
 * order's goods total.
 */
final class VolumeDiscountHandler extends PricingHandler {
	private static final int THRESHOLD = 100;
	private static final BigDecimal RATE = new BigDecimal("-0.20");

	@Override
	protected void apply(PricingState state) {
		if (state.totalUnits() > THRESHOLD) {
			state.applyOrderDiscount(
					PriceAdjustment.ofRate("Bulk order (>" + THRESHOLD + " units)", state.goodsTotal(), RATE), RATE);
		}
	}
}

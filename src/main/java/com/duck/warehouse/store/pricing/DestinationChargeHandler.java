package com.duck.warehouse.store.pricing;

/**
 * Stage 4 - rules 6-9, once per ORDER: destination surcharge on the order base.
 * Rates live in the Destination enum.
 */
final class DestinationChargeHandler extends PricingHandler {
	@Override
	protected void apply(PricingState state) {
		var destination = state.request().destination();
		state.addOrderAdjustment(PriceAdjustment.ofRate("Destination " + destination.name(), state.orderBase(),
				destination.surchargeRate()));
	}
}

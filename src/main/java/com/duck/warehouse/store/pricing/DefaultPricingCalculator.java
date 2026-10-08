package com.duck.warehouse.store.pricing;

import org.springframework.stereotype.Service;

/**
 * Wires the chain. The order of the links below IS the order of the pricing
 * stages: 1 base price -> 2 volume discount -> 3 package cost -> 4 destination
 * charge -> 5 shipping charge (end) Stages 1, 3 work line by line; stages 2, 4,
 * 5 work on the order as a whole.
 */
@Service
public class DefaultPricingCalculator implements PricingCalculator {

	private final PricingHandler chain;

	public DefaultPricingCalculator() {
		PricingHandler first = new BasePriceHandler();
		first.linkWith(new VolumeDiscountHandler()).linkWith(new PackageCostHandler())
				.linkWith(new DestinationChargeHandler()).linkWith(new ShippingChargeHandler()); // last link: nothing
																									// after it, the
																									// chain stops here
		this.chain = first;
	}

	@Override
	public PriceBreakdown calculate(PricingRequest request) {
		PricingState state = new PricingState(request); // fresh state per call -> thread-safe
		chain.handle(state);
		return state.toBreakdown();
	}
}

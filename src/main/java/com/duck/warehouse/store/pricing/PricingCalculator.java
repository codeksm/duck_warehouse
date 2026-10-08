package com.duck.warehouse.store.pricing;

/**
 * Public contract of the pricing module. OrderService depends only on this
 * interface, so the pricing algorithm can be replaced (or decorated, or stubbed
 * in tests) without touching the order flow. Prices a whole order; see
 * {@link PriceBreakdown} for the shape of the result.
 */
public interface PricingCalculator {
	PriceBreakdown calculate(PricingRequest request);
}

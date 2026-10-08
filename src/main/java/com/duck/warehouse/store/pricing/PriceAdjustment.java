package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;

/**
 * A signed line of the breakdown: negative = discount, positive = increment.
 */
public record PriceAdjustment(String name, BigDecimal amount) {

	/**
	 * Percentage-of-base adjustment; the name states discount/surcharge and the
	 * rate.
	 */
	static PriceAdjustment ofRate(String subject, BigDecimal base, BigDecimal rate) {
		String kind = rate.signum() < 0 ? "discount" : "surcharge";
		String pct = rate.abs().movePointRight(2).stripTrailingZeros().toPlainString();
		return new PriceAdjustment(subject + " " + kind + " (" + pct + "%)", Money.percentOf(base, rate));
	}

	static PriceAdjustment flat(String name, BigDecimal amount) {
		return new PriceAdjustment(name, Money.round(amount));
	}
}

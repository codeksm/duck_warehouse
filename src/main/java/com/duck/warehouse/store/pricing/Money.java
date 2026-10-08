package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * All money is BigDecimal, 2 decimals, HALF_UP. Every adjustment is rounded
 * individually so the breakdown sums exactly to the total.
 */
final class Money {
	private Money() {
	}

	static BigDecimal round(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	static BigDecimal percentOf(BigDecimal base, BigDecimal rate) {
		return round(base.multiply(rate));
	}
}

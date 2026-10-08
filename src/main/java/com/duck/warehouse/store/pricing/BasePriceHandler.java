package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;

/** Stage 1 - rule 1: goods cost = sum over lines of (quantity x unit price). */
final class BasePriceHandler extends PricingHandler {
	@Override
	protected void apply(PricingState state) {
		BigDecimal total = BigDecimal.ZERO;
		for (PricingState.LineState line : state.lines()) {
			PricingLine in = line.input();
			line.setGoodsCost(Money.round(in.unitPrice().multiply(BigDecimal.valueOf(in.quantity()))));
			total = total.add(line.goodsCost());
		}
		state.setGoodsTotal(total);
	}
}

package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The running calculation passed along the chain. Created per request and never
 * shared, so it is safe for it to be mutable.
 *
 * "orderBase" is the amount order-level percentage stages apply to: goods total
 * minus the bulk discount. Line-level percentage stages use the same idea per
 * line (see {@link #lineBase}).
 */
final class PricingState {

	/** Mutable working copy of one order line. */
	static final class LineState {
		private final PricingLine input;
		private final List<PriceAdjustment> adjustments = new ArrayList<>();
		private BigDecimal goodsCost = BigDecimal.ZERO;

		LineState(PricingLine input) {
			this.input = input;
		}

		PricingLine input() {
			return input;
		}

		BigDecimal goodsCost() {
			return goodsCost;
		}

		void setGoodsCost(BigDecimal amount) {
			this.goodsCost = amount;
		}

		void add(PriceAdjustment adjustment) {
			adjustments.add(adjustment);
		}

		LineBreakdown toBreakdown() {
			BigDecimal total = goodsCost;
			for (PriceAdjustment a : adjustments)
				total = total.add(a.amount());
			return new LineBreakdown(goodsCost, List.copyOf(adjustments), total);
		}
	}

	private final PricingRequest request;
	private final List<LineState> lines;
	private final List<PriceAdjustment> orderAdjustments = new ArrayList<>();
	private BigDecimal goodsTotal = BigDecimal.ZERO;
	private BigDecimal orderBase = BigDecimal.ZERO;
	private BigDecimal discountRate = BigDecimal.ZERO; // e.g. -0.20 once the bulk discount applied

	PricingState(PricingRequest request) {
		this.request = request;
		this.lines = request.lines().stream().map(LineState::new).toList();
	}

	PricingRequest request() {
		return request;
	}

	List<LineState> lines() {
		return lines;
	}

	int totalUnits() {
		return request.totalUnits();
	}

	BigDecimal goodsTotal() {
		return goodsTotal;
	}

	BigDecimal orderBase() {
		return orderBase;
	}

	void setGoodsTotal(BigDecimal amount) {
		this.goodsTotal = amount;
		this.orderBase = amount;
	}

	/**
	 * The order-level discount: also lowers the base later percentage stages use.
	 */
	void applyOrderDiscount(PriceAdjustment adjustment, BigDecimal rate) {
		orderAdjustments.add(adjustment);
		orderBase = orderBase.add(adjustment.amount());
		discountRate = rate;
	}

	/** Order-level charge that does not change the base (destination, shipping). */
	void addOrderAdjustment(PriceAdjustment adjustment) {
		orderAdjustments.add(adjustment);
	}

	/**
	 * A line's goods cost after its share of the order discount (same rate for
	 * every line).
	 */
	BigDecimal lineBase(LineState line) {
		return Money.round(line.goodsCost().multiply(BigDecimal.ONE.add(discountRate)));
	}

	PriceBreakdown toBreakdown() {
		List<LineBreakdown> lineResults = lines.stream().map(LineState::toBreakdown).toList();
		BigDecimal total = goodsTotal;
		for (LineBreakdown l : lineResults) {
			for (PriceAdjustment a : l.adjustments())
				total = total.add(a.amount());
		}
		for (PriceAdjustment a : orderAdjustments)
			total = total.add(a.amount());
		return new PriceBreakdown(goodsTotal, lineResults, List.copyOf(orderAdjustments), total);
	}
}

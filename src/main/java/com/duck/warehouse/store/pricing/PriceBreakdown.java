package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * Result for a whole order. Always: total = goodsCost + all line adjustments +
 * all order adjustments, i.e. sum(line totals) + order adjustments.
 * {@code lines} are in the same order as the request lines.
 */
public record PriceBreakdown(BigDecimal goodsCost, List<LineBreakdown> lines, List<PriceAdjustment> orderAdjustments,
		BigDecimal total) {
}

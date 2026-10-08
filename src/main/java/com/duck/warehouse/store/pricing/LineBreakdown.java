package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * Price of one line: goods cost plus the line-level adjustments (its package
 * cost). Order-level charges are not included.
 */
public record LineBreakdown(BigDecimal goodsCost, List<PriceAdjustment> adjustments, BigDecimal total) {
}

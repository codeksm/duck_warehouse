package com.duck.warehouse.store.pricing;

import java.math.BigDecimal;

import com.duck.warehouse.store.domain.PackageType;

/**
 * One order line as pricing sees it: how many, at what unit price, in which
 * package.
 */
public record PricingLine(int quantity, BigDecimal unitPrice, PackageType packageType) {
}

package com.duck.warehouse.store.packaging;

import java.util.List;

import com.duck.warehouse.store.domain.PackageType;
import com.duck.warehouse.store.domain.ProtectionType;
import com.duck.warehouse.store.domain.ShippingMode;

/** Strategy: how a given shipping mode protects a given package (rules 4-7). */
public interface ProtectionStrategy {
	ShippingMode mode();

	List<ProtectionType> protectionFor(PackageType packageType);
}

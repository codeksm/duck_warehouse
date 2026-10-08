package com.duck.warehouse.store.packaging;

import org.springframework.stereotype.Service;

import com.duck.warehouse.shared.Size;
import com.duck.warehouse.store.domain.PackageType;
import com.duck.warehouse.store.domain.ShippingMode;

@Service
public class PackagingService {
	private final PackageSelector selector;
	private final ProtectionResolver protections;

	public PackagingService(PackageSelector selector, ProtectionResolver protections) {
		this.selector = selector;
		this.protections = protections;
	}

	public Packaging plan(Size size, ShippingMode mode) {
		PackageType packageType = selector.select(size);
		return new Packaging(packageType, protections.resolve(mode, packageType));
	}
}

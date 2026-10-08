package com.duck.warehouse.store.packaging;

import java.util.List;
import org.springframework.stereotype.Component;

import com.duck.warehouse.store.domain.*;
import com.duck.warehouse.store.domain.ShippingMode;

@Component
public class SeaProtectionStrategy implements ProtectionStrategy {
	@Override
	public ShippingMode mode() {
		return ShippingMode.SEA;
	}

	@Override
	public List<ProtectionType> protectionFor(PackageType packageType) {
		return List.of(ProtectionType.MOISTURE_ABSORBING_BEADS, ProtectionType.BUBBLE_WRAP_BAGS);
	}
}

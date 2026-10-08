package com.duck.warehouse.store.packaging;

import java.util.List;
import org.springframework.stereotype.Component;

import com.duck.warehouse.store.domain.*;
import com.duck.warehouse.store.domain.ShippingMode;

@Component
public class AirProtectionStrategy implements ProtectionStrategy {
	@Override
	public ShippingMode mode() {
		return ShippingMode.AIR;
	}

	@Override
	public List<ProtectionType> protectionFor(PackageType packageType) {
		return switch (packageType) {
		case WOOD, CARDBOARD -> List.of(ProtectionType.POLYSTYRENE_BALLS);
		case PLASTIC -> List.of(ProtectionType.BUBBLE_WRAP_BAGS);
		};
	}
}

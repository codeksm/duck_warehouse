package com.duck.warehouse.store;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import static com.duck.warehouse.store.domain.ProtectionType.*;

import com.duck.warehouse.store.packaging.AirProtectionStrategy;
import com.duck.warehouse.shared.Size;
import com.duck.warehouse.store.domain.PackageType;
import com.duck.warehouse.store.domain.ShippingMode;
import com.duck.warehouse.store.packaging.*;

class PackagingServiceTest {
	private final PackagingService service = new PackagingService(new PackageSelector(), new ProtectionResolver(
			List.of(new AirProtectionStrategy(), new LandProtectionStrategy(), new SeaProtectionStrategy())));

	@Test
	void sizeDeterminesPackage() {
		assertThat(service.plan(Size.XLARGE, ShippingMode.LAND).packageType()).isEqualTo(PackageType.WOOD);
		assertThat(service.plan(Size.LARGE, ShippingMode.LAND).packageType()).isEqualTo(PackageType.WOOD);
		assertThat(service.plan(Size.MEDIUM, ShippingMode.LAND).packageType()).isEqualTo(PackageType.CARDBOARD);
		assertThat(service.plan(Size.SMALL, ShippingMode.LAND).packageType()).isEqualTo(PackageType.PLASTIC);
		assertThat(service.plan(Size.XSMALL, ShippingMode.LAND).packageType()).isEqualTo(PackageType.PLASTIC);
	}

	@Test
	void airProtectionDependsOnPackage() {
		assertThat(service.plan(Size.LARGE, ShippingMode.AIR).protections()).containsExactly(POLYSTYRENE_BALLS);
		assertThat(service.plan(Size.MEDIUM, ShippingMode.AIR).protections()).containsExactly(POLYSTYRENE_BALLS);
		assertThat(service.plan(Size.SMALL, ShippingMode.AIR).protections()).containsExactly(BUBBLE_WRAP_BAGS);
	}

	@Test
	void landAndSeaProtectionIgnorePackage() {
		for (Size s : Size.values()) {
			assertThat(service.plan(s, ShippingMode.LAND).protections()).containsExactly(POLYSTYRENE_BALLS);
			assertThat(service.plan(s, ShippingMode.SEA).protections()).containsExactly(MOISTURE_ABSORBING_BEADS,
					BUBBLE_WRAP_BAGS);
		}
	}
}

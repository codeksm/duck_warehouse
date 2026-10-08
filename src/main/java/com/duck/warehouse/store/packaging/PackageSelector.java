package com.duck.warehouse.store.packaging;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

import com.duck.warehouse.shared.*;
import com.duck.warehouse.store.domain.PackageType;

/**
 * Rules 1-3: size -> package. A table instead of if/else; the constructor
 * guarantees every size is covered.
 */
@Component
public class PackageSelector {
	private final Map<Size, PackageType> bySize = new EnumMap<>(Size.class);

	public PackageSelector() {
		bySize.put(Size.XLARGE, PackageType.WOOD);
		bySize.put(Size.LARGE, PackageType.WOOD);
		bySize.put(Size.MEDIUM, PackageType.CARDBOARD);
		bySize.put(Size.SMALL, PackageType.PLASTIC);
		bySize.put(Size.XSMALL, PackageType.PLASTIC);
		for (Size s : Size.values()) {
			if (!bySize.containsKey(s))
				throw new IllegalStateException("No package defined for size " + s);
		}
	}

	public PackageType select(Size size) {
		return bySize.get(size);
	}
}

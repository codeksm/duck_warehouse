package com.duck.warehouse.store.packaging;

import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.duck.warehouse.store.domain.*;

/**
 * Registry of strategies keyed by mode. New shipping mode = new strategy bean,
 * no edits here.
 */
@Component
public class ProtectionResolver {
	private final Map<ShippingMode, ProtectionStrategy> strategies = new EnumMap<>(ShippingMode.class);

	public ProtectionResolver(List<ProtectionStrategy> all) {
		all.forEach(s -> strategies.put(s.mode(), s));
		for (ShippingMode m : ShippingMode.values()) {
			if (!strategies.containsKey(m))
				throw new IllegalStateException("No protection strategy for " + m);
		}
	}

	public List<ProtectionType> resolve(ShippingMode mode, PackageType packageType) {
		return strategies.get(mode).protectionFor(packageType);
	}
}
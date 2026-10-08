package com.duck.warehouse.store.domain;

import com.duck.warehouse.shared.Labeled;

public enum ShippingMode implements Labeled {
	LAND("Land"), AIR("Air"), SEA("Sea");

	private final String label;

	ShippingMode(String label) {
		this.label = label;
	}

	@Override
	public String label() {
		return label;
	}
}

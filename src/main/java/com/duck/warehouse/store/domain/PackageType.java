package com.duck.warehouse.store.domain;

import com.duck.warehouse.shared.Labeled;

public enum PackageType implements Labeled {
	WOOD("Wood"), CARDBOARD("Cardboard"), PLASTIC("Plastic");

	private final String label;

	PackageType(String label) {
		this.label = label;
	}

	@Override
	public String label() {
		return label;
	}
}

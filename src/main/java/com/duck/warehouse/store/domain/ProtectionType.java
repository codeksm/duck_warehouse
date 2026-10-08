package com.duck.warehouse.store.domain;

import com.duck.warehouse.shared.Labeled;

public enum ProtectionType implements Labeled {
	POLYSTYRENE_BALLS("Polystyrene balls"), BUBBLE_WRAP_BAGS("Bubble-wrap bags"),
	MOISTURE_ABSORBING_BEADS("Moisture-absorbing beads");

	private final String label;

	ProtectionType(String label) {
		this.label = label;
	}

	@Override
	public String label() {
		return label;
	}
}

package com.duck.warehouse.shared;

public enum Color implements Labeled {
	RED("Red"), GREEN("Green"), YELLOW("Yellow"), BLACK("Black");

	private final String label;

	Color(String label) {
		this.label = label;
	}

	@Override
	public String label() {
		return label;
	}
}

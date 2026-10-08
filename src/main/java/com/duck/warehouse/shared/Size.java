package com.duck.warehouse.shared;

public enum Size implements Labeled {
	XLARGE("XLarge"), LARGE("Large"), MEDIUM("Medium"), SMALL("Small"), XSMALL("XSmall");

	private final String label;

	Size(String label) {
		this.label = label;
	}

	@Override
	public String label() {
		return label;
	}
}

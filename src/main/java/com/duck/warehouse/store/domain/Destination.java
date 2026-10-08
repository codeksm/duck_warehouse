package com.duck.warehouse.store.domain;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Destination surcharge table (rules 6-9). Adding a country = adding one
 * constant.
 */
public enum Destination {
	USA("0.18", "USA", "US", "UNITED STATES", "UNITED STATES OF AMERICA"), BOLIVIA("0.13", "BOLIVIA"),
	INDIA("0.19", "INDIA"), OTHER("0.15");

	private final BigDecimal surchargeRate;
	private final String[] aliases;

	Destination(String rate, String... aliases) {
		this.surchargeRate = new BigDecimal(rate);
		this.aliases = aliases;
	}

	public BigDecimal surchargeRate() {
		return surchargeRate;
	}

	/** Case/whitespace-insensitive; anything unrecognised is OTHER. */
	public static Destination from(String country) {
		String normalized = country.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
		for (Destination d : values()) {
			for (String alias : d.aliases) {
				if (alias.equals(normalized))
					return d;
			}
		}
		return OTHER;
	}
}

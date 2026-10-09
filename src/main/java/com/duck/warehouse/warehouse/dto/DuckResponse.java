package com.duck.warehouse.warehouse.dto;

import com.duck.warehouse.warehouse.domain.Duck;

public record DuckResponse(Integer id, String color, String size, Double price, Integer quantity, Boolean deleted) {
	public static DuckResponse from(Duck d) {
		return new DuckResponse(d.id(), d.color().label(), d.size().label(), d.price(), d.quantity(), d.deleted());
	}
}
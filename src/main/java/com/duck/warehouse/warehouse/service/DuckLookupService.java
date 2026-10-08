package com.duck.warehouse.warehouse.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

import com.duck.warehouse.shared.Color;
import com.duck.warehouse.shared.Size;
import com.duck.warehouse.exception.*;
import com.duck.warehouse.warehouse.repo.DuckRepository;
import com.duck.warehouse.warehouse.spi.AvailableDuck;
import com.duck.warehouse.warehouse.spi.WarehouseLookup;

@Service
class DuckLookupService implements WarehouseLookup {
	private final DuckRepository ducks;

	DuckLookupService(DuckRepository ducks) {
		this.ducks = ducks;
	}

	@Override
	public AvailableDuck findCheapestAvailable(Color color, Size size, int quantity) {
		return ducks
				.findFirstByColorAndSizeAndDeletedFalseAndQuantityGreaterThanEqualOrderByPriceAsc(color, size, quantity)
				.map(d -> new AvailableDuck(d.id(), BigDecimal.valueOf(d.price())))
				.orElseThrow(() -> ducks.existsByColorAndSizeAndDeletedFalse(color, size)
						? new ConflictException("Insufficient stock: no single " + color.label() + " " + size.label()
								+ " price tier holds " + quantity + " units")
						: new NotFoundException(
								"No " + color.label() + " " + size.label() + " ducks in the warehouse"));
	}
}

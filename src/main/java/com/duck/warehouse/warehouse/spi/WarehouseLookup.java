package com.duck.warehouse.warehouse.spi;

import com.duck.warehouse.shared.Color;
import com.duck.warehouse.shared.Size;

/** The only warehouse API the store module may depend on. */
public interface WarehouseLookup {
	/**
	 * Price policy: the cheapest live record of this color/size that alone holds
	 * the requested quantity.
	 *
	 * @throws com.duck.warehouse.exception.NotFoundException if no live duck of this
	 *                                                color/size exists
	 * @throws com.duck.warehouse.exception.ConflictException if some exist but none has
	 *                                                enough stock
	 */
	AvailableDuck findCheapestAvailable(Color color, Size size, int quantity);
}
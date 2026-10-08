package com.duck.warehouse.warehouse.domain;

import org.springframework.data.mongodb.core.mapping.Document;

import com.duck.warehouse.shared.Color;
import com.duck.warehouse.shared.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Warehouse stock record. Uniqueness of (color, size, price) among non-deleted
 * records is enforced by a partial unique index (see
 * WarehouseIndexInitializer), not by application code.
 */
@Document("ducks")
public record Duck(@Id Integer id, Color color, Size size, Double price, Integer quantity, Boolean deleted) {
}

package com.duck.warehouse.store.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duck.warehouse.store.dto.OrderRequest;
import com.duck.warehouse.store.dto.OrderResponse;
import com.duck.warehouse.store.service.OrderService;

@RestController
@RequestMapping("/api/store/orders")
class OrderController {
	private final OrderService service;

	OrderController(OrderService service) {
		this.service = service;
	}

	@PostMapping
	OrderResponse quote(@Valid @RequestBody OrderRequest request) {
		return service.quote(request);
	}
}
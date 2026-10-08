package com.duck.warehouse.warehouse.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duck.warehouse.warehouse.dto.AddDuckRequest;
import com.duck.warehouse.warehouse.dto.DuckResponse;
import com.duck.warehouse.warehouse.dto.PageResponse;
import com.duck.warehouse.warehouse.dto.UpdateDuckRequest;
import com.duck.warehouse.warehouse.repo.DuckWriteRepository.AddResult;
import com.duck.warehouse.warehouse.service.DuckService;

@RestController
@RequestMapping("/api/ducks")
public class DuckController {

	private final DuckService service;

	DuckController(DuckService service) {
		this.service = service;
	}

	/**
	 * 201 when a new record was created, 200 when quantities were merged into an
	 * existing one.
	 */
	@PostMapping
	ResponseEntity<DuckResponse> add(@Valid @RequestBody AddDuckRequest request) {
		AddResult result = service.add(request);
		DuckResponse body = DuckResponse.from(result.duck());
		if (result.created()) {
			return ResponseEntity.created(URI.create("/api/ducks/" + body.id())).body(body);
		}
		return ResponseEntity.ok(body);
	}

	@GetMapping
	PageResponse<DuckResponse> list(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "quantity") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) {
		return PageResponse.from(service.list(page, size, sortBy, direction), DuckResponse::from);
	}

	@PatchMapping("/{id}")
	DuckResponse update(@PathVariable int id, @Valid @RequestBody UpdateDuckRequest request) {
		return DuckResponse.from(service.update(id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable int id) {
		service.delete(id);
	}
}

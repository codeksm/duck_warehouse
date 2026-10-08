package com.duck.warehouse.warehouse.service;

import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.duck.warehouse.shared.Color;
import com.duck.warehouse.exception.InvalidInputException;
import com.duck.warehouse.exception.NotFoundException;
import com.duck.warehouse.shared.*;
import com.duck.warehouse.warehouse.dto.*;
import com.duck.warehouse.warehouse.domain.*;
import com.duck.warehouse.warehouse.dto.AddDuckRequest;
import com.duck.warehouse.warehouse.repo.DuckRepository;
import com.duck.warehouse.warehouse.repo.DuckWriteRepository;
import com.duck.warehouse.warehouse.repo.DuckWriteRepository.AddResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DuckService {
	static final int MAX_PAGE_SIZE = 100;
	private static final Set<String> SORTABLE = Set.of("id", "color", "size", "price", "quantity");

	private final DuckRepository reads;
	private final DuckWriteRepository writes;

	DuckService(DuckRepository reads, DuckWriteRepository writes) {
		this.reads = reads;
		this.writes = writes;
	}

	public AddResult add(AddDuckRequest request) {
		Color color = EnumParser.parse(Color.class, "color", request.color());
		Size size = EnumParser.parse(Size.class, "size", request.size());
		return writes.addOrMerge(color, size, request.price(), request.quantity());
	}

	public Page<Duck> list(int page, int size, String sortBy, String direction) {
		if (page < 0)
			throw new InvalidInputException("page must be >= 0");
		if (size < 1 || size > MAX_PAGE_SIZE)
			throw new InvalidInputException("size must be between 1 and " + MAX_PAGE_SIZE);
		if (!SORTABLE.contains(sortBy))
			throw new InvalidInputException("sortBy must be one of " + SORTABLE);
		Sort.Direction dir = switch (direction.toLowerCase()) {
		case "asc" -> Sort.Direction.ASC;
		case "desc" -> Sort.Direction.DESC;
		default -> throw new InvalidInputException("direction must be 'asc' or 'desc'");
		};
		// id as tie-breaker keeps pagination stable when many ducks share a quantity.
		Sort sort = Sort.by(dir, sortBy).and(Sort.by(Sort.Direction.ASC, "id"));
		return reads.findByDeletedFalse(PageRequest.of(page, size, sort));
	}

	public Duck update(int id, UpdateDuckRequest request) {
		if (request.price() == null && request.quantity() == null) {
			throw new InvalidInputException("Provide at least one of: price, quantity");
		}
		return writes.update(id, request.price(), request.quantity())
				.orElseThrow(() -> new NotFoundException("Duck " + id + " not found"));
	}

	public void delete(int id) {
		if (!writes.softDelete(id))
			throw new NotFoundException("Duck " + id + " not found");
	}
}

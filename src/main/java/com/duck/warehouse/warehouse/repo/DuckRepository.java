package com.duck.warehouse.warehouse.repo;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.duck.warehouse.shared.Color;
import com.duck.warehouse.shared.Size;
import com.duck.warehouse.warehouse.domain.Duck;

/**
 * Read side. All writes go through {@link DuckWriteRepository} because they
 * must be atomic.
 */
public interface DuckRepository extends MongoRepository<Duck, Integer> {

	Page<Duck> findByDeletedFalse(Pageable pageable);

	Optional<Duck> findFirstByColorAndSizeAndDeletedFalseAndQuantityGreaterThanEqualOrderByPriceAsc(Color color,
			Size size, Integer quantity);

	boolean existsByColorAndSizeAndDeletedFalse(Color color, Size size);
}
package com.duck.warehouse.warehouse.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.duck.warehouse.warehouse.domain.Duck;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Creates the invariant behind the merge rule: at most ONE non-deleted duck per
 * (color, size, price). Logically deleted records are excluded so a deleted
 * duck never blocks re-adding the same variant.
 */
@Component
class WarehouseIndexInitializer implements ApplicationRunner {
	private final MongoTemplate template;
	private static final Logger log = LoggerFactory.getLogger(WarehouseIndexInitializer.class);

	WarehouseIndexInitializer(MongoTemplate template) {
		this.template = template;
	}
	
	@Override
	public void run(ApplicationArguments args) {
	    try {
	    	template.indexOps(Duck.class)
			.createIndex(new Index().named("uq_active_duck").on("color", Sort.Direction.ASC)
					.on("size", Sort.Direction.ASC).on("price", Sort.Direction.ASC).unique()
					.partial(PartialIndexFilter.of(Criteria.where("deleted").is(false))));
	        log.info("Unique index 'uq_active_duck' ensured on ducks(color, size, price) where deleted=false");
	    } catch (RuntimeException e) {
	        log.error("Could not create the unique index; the merge invariant is NOT protected", e);
	        throw e;   // fail startup rather than run without the invariant
	    }
	}
}
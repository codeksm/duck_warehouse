package com.duck.warehouse.warehouse;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/**
 * Atomic Integer id generator (Mongo has no native integer auto-increment).
 * Gaps are possible and harmless.
 */
@Component
public class IdSequenceGenerator implements IdGenerator {
	private static final String COLLECTION = "counters";
	private final MongoTemplate template;

	IdSequenceGenerator(MongoTemplate template) {
		this.template = template;
	}

	@Override
	public int next() {
		Document doc = template.findAndModify(Query.query(Criteria.where("_id").is("duck")), new Update().inc("seq", 1),
				FindAndModifyOptions.options().upsert(true).returnNew(true), Document.class, COLLECTION);
		return doc.getInteger("seq");
	}

}

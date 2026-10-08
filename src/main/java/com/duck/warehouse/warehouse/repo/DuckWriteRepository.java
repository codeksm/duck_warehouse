package com.duck.warehouse.warehouse.repo;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.duck.warehouse.shared.Color;
import com.duck.warehouse.shared.Size;
import com.duck.warehouse.warehouse.IdGenerator;
import com.duck.warehouse.warehouse.domain.Duck;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * Atomic, single-statement writes. No read-modify-write cycles, so concurrent
 * callers cannot lose updates.
 */
@Repository
public class DuckWriteRepository {
	private static final int MAX_ATTEMPTS = 5;

	private final MongoTemplate template;
	private final IdGenerator ids;

	DuckWriteRepository(MongoTemplate template, IdGenerator ids) {
		this.template = template;
		this.ids = ids;
	}

	public record AddResult(Duck duck, boolean created) {
	}

	/**
	 * Upsert + $inc: one atomic statement. Two concurrent adds of the same duck
	 * either both increment the same document, or one inserts and the other hits
	 * the unique index and retries as an increment.
	 */
	public AddResult addOrMerge(Color color, Size size, double price, int quantity) {
		for (int attempt = 1;; attempt++) {
			int candidateId = ids.next();
			Query query = Query.query(Criteria.where("color").is(color).and("size").is(size).and("price").is(price)
					.and("deleted").is(false));
			Update update = new Update().inc("quantity", quantity).setOnInsert("id", candidateId);
			try {
				Duck previous = template.findAndModify(query, update,
						FindAndModifyOptions.options().upsert(true).returnNew(false), Duck.class);
				return previous == null
						? new AddResult(new Duck(candidateId, color, size, price, quantity, false), true)
						: new AddResult(
								new Duck(previous.id(), color, size, price, previous.quantity() + quantity, false),
								false);
			} catch (DuplicateKeyException race) {
				if (attempt >= MAX_ATTEMPTS)
					throw race;
			}
		}
	}

	/**
	 * Sets only the provided fields; empty if the duck does not exist or is
	 * deleted.
	 */
	public Optional<Duck> update(int id, Double price, Integer quantity) {
		Update update = new Update();
		if (price != null)
			update.set("price", price);
		if (quantity != null)
			update.set("quantity", quantity);
		return Optional.ofNullable(template.findAndModify(activeById(id), update,
				FindAndModifyOptions.options().returnNew(true), Duck.class));
	}

	/** @return true if a live duck was marked deleted. */
	public boolean softDelete(int id) {
		return template.findAndModify(activeById(id), new Update().set("deleted", true),
				FindAndModifyOptions.options().returnNew(false), Duck.class) != null;
	}

	private static Query activeById(int id) {
		return Query.query(Criteria.where("_id").is(id).and("deleted").is(false));
	}
}

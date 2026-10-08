package com.duck.warehouse.store.pricing;

/** Chain of Responsibility: do your own stage, then pass the state to the next handler (if any). */
abstract class PricingHandler {
    private PricingHandler next;

    /** Fluent linking: returns the handler just linked, so calls can be chained in order. */
    PricingHandler linkWith(PricingHandler next) {
        this.next = next;
        return next;
    }

    final void handle(PricingState state) {
        apply(state);
        if (next != null) {
            next.handle(state);
        }
    }

    /** This stage's own work. */
	protected abstract void apply(PricingState state);
}

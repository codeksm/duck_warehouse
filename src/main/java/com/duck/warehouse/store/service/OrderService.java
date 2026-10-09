package com.duck.warehouse.store.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.duck.warehouse.shared.Color;
import com.duck.warehouse.shared.EnumParser;
import com.duck.warehouse.shared.Size;
import com.duck.warehouse.store.domain.Destination;
import com.duck.warehouse.store.domain.ProtectionType;
import com.duck.warehouse.store.domain.ShippingMode;
import com.duck.warehouse.store.dto.OrderItemRequest;
import com.duck.warehouse.store.dto.OrderRequest;
import com.duck.warehouse.store.dto.OrderResponse;
import com.duck.warehouse.store.dto.OrderResponse.AdjustmentResponse;
import com.duck.warehouse.store.dto.OrderResponse.OrderLineResponse;
import com.duck.warehouse.store.packaging.Packaging;
import com.duck.warehouse.store.packaging.PackagingService;
import com.duck.warehouse.store.pricing.LineBreakdown;
import com.duck.warehouse.store.pricing.PriceAdjustment;
import com.duck.warehouse.store.pricing.PriceBreakdown;
import com.duck.warehouse.store.pricing.PricingCalculator;
import com.duck.warehouse.store.pricing.PricingLine;
import com.duck.warehouse.store.pricing.PricingRequest;
import com.duck.warehouse.warehouse.spi.AvailableDuck;
import com.duck.warehouse.warehouse.spi.WarehouseLookup;

/**
 * Orchestration only: 1. for each item: resolve the unit price from the
 * warehouse and decide its packaging 2. price the WHOLE order once (pricing
 * module) 3. map the result to the response Read-only quote; stock is not
 * reserved.
 */
@Service
public class OrderService {
	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	/** An order item after price resolution and packaging. */
	private record ResolvedItem(Color color, Size size, int quantity, BigDecimal unitPrice, Packaging packaging) {
	}

	private final WarehouseLookup warehouse;
	private final PackagingService packaging;
	private final PricingCalculator calculator;

	public OrderService(WarehouseLookup warehouse, PackagingService packaging, PricingCalculator calculator) {
		this.warehouse = warehouse;
		this.packaging = packaging;
		this.calculator = calculator;
	}

	public OrderResponse quote(OrderRequest request) {
		Destination destination = Destination.from(request.country());
		ShippingMode mode = EnumParser.parse(ShippingMode.class, "shippingMode", request.shippingMode());

		List<ResolvedItem> items = request.items().stream().map(i -> resolve(i, mode)).toList();

		PricingRequest pricingRequest = new PricingRequest(destination, mode, items.stream()
				.map(i -> new PricingLine(i.quantity(), i.unitPrice(), i.packaging().packageType())).toList());
		PriceBreakdown price = calculator.calculate(pricingRequest);

		List<OrderLineResponse> lines = IntStream.range(0, items.size())
				.mapToObj(n -> toLine(items.get(n), price.lines().get(n))).toList();
		
		log.info("Order quoted: items={} units={} destination={} mode={} total={}",
		        items.size(), pricingRequest.totalUnits(), destination, mode.label(), price.total());
		return new OrderResponse(request.country().trim(), mode.label(), "USD", pricingRequest.totalUnits(), lines,
				price.goodsCost(), toAdjustments(price.orderAdjustments()), price.total());
	}

	private ResolvedItem resolve(OrderItemRequest item, ShippingMode mode) {
		Color color = EnumParser.parse(Color.class, "color", item.color());
		Size size = EnumParser.parse(Size.class, "size", item.size());
		int quantity = item.quantity();
		AvailableDuck duck = warehouse.findCheapestAvailable(color, size, quantity);
		return new ResolvedItem(color, size, quantity, duck.unitPrice(), packaging.plan(size, mode));
	}

	private static OrderLineResponse toLine(ResolvedItem item, LineBreakdown price) {
		return new OrderLineResponse(item.color().label(), item.size().label(), item.quantity(),
				item.unitPrice().setScale(2, RoundingMode.HALF_UP), item.packaging().packageType().label(),
				item.packaging().protections().stream().map(ProtectionType::label).toList(), price.goodsCost(),
				toAdjustments(price.adjustments()), price.total());
	}

	private static List<AdjustmentResponse> toAdjustments(List<PriceAdjustment> adjustments) {
		return adjustments.stream().map(a -> new AdjustmentResponse(a.name(), a.amount())).toList();
	}
}

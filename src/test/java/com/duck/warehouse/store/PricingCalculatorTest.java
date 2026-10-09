package com.duck.warehouse.store;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.duck.warehouse.store.pricing.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.duck.warehouse.store.domain.Destination;
import com.duck.warehouse.store.domain.PackageType;
import com.duck.warehouse.store.domain.ShippingMode;
import com.duck.warehouse.store.pricing.PriceBreakdown;
import com.duck.warehouse.store.pricing.PricingLine;
import com.duck.warehouse.store.pricing.PricingRequest;

class PricingCalculatorTest {

	private final PricingCalculator calculator = new DefaultPricingCalculator();

	private static PricingLine line(int qty, String unit, PackageType p) {
		return new PricingLine(qty, new BigDecimal(unit), p);
	}

	private PriceBreakdown price(Destination d, ShippingMode m, PricingLine... lines) {
		return calculator.calculate(new PricingRequest(d, m, Arrays.asList(lines)));
	}

	private static List<String> names(List<PriceAdjustment> adjustments) {
		return adjustments.stream().map(PriceAdjustment::name).toList();
	}

	// ---------- single-line orders: identical to the literal spec ----------

	@Test
	void singleLineLandOrder_stagesRunInOrder() {
		// 10 x 5.00 = 50.00; wood +2.50 (line); USA +9.00; land 10 units x 10 = +100.00
		// (order)
		PriceBreakdown b = price(Destination.USA, ShippingMode.LAND, line(10, "5.00", PackageType.WOOD));
		assertThat(b.goodsCost()).isEqualByComparingTo("50.00");
		assertThat(b.lines().get(0).adjustments().get(0).amount()).isEqualByComparingTo("2.50");
		assertThat(b.lines().get(0).total()).isEqualByComparingTo("52.50");
		assertThat(b.orderAdjustments()).hasSize(2);
		assertThat(b.orderAdjustments().get(0).amount()).isEqualByComparingTo("9.00");
		assertThat(b.orderAdjustments().get(1).amount()).isEqualByComparingTo("100.00");
		assertThat(b.total()).isEqualByComparingTo("161.50");
	}

	@Test
	void volumeDiscount_singleLine_strictlyAboveHundred() {
		// 100: 100 -1.00 (cardboard) +15.00 +400 = 514.00
		assertThat(price(Destination.OTHER, ShippingMode.SEA, line(100, "1.00", PackageType.CARDBOARD)).total())
				.isEqualByComparingTo("514.00");
		// 101: 101 -20.20 (base 80.80) -0.81 +12.12 +400 = 492.11
		assertThat(price(Destination.OTHER, ShippingMode.SEA, line(101, "1.00", PackageType.CARDBOARD)).total())
				.isEqualByComparingTo("492.11");
	}

	@Test
	void airBulkReduction_singleLine_strictlyAboveThousand() {
		// 1000: 1000 -200 (base 800) +80 +152 +30000 = 31032.00
		assertThat(price(Destination.INDIA, ShippingMode.AIR, line(1000, "1.00", PackageType.PLASTIC)).total())
				.isEqualByComparingTo("31032.00");
		// 1001: 1001 -200.20 (base 800.80) +80.08 +152.15 +30030 -4504.50 = 26558.53
		assertThat(price(Destination.INDIA, ShippingMode.AIR, line(1001, "1.00", PackageType.PLASTIC)).total())
				.isEqualByComparingTo("26558.53");
	}

	// ---------- multi-line orders: thresholds and charges are per ORDER ----------

	@Test
	void bulkDiscount_usesTotalUnitsOfTheOrder_notEachLine() {
		// 3 lines x 40 units = 120 units (each line alone is below 100). Small =
		// plastic, USA, Air.
		PriceBreakdown b = price(Destination.USA, ShippingMode.AIR, line(40, "10.00", PackageType.PLASTIC),
				line(40, "10.00", PackageType.PLASTIC), line(40, "10.00", PackageType.PLASTIC));
		// goods 1200.00; bulk -240.00 (base 960); plastic +10% of each line's 320.00 =
		// +32.00 x 3;
		// USA 18% of 960 = +172.80; air 120 x 30 = +3600.00 (no air reduction: 120 <=
		// 1000)
		assertThat(b.goodsCost()).isEqualByComparingTo("1200.00");
		assertThat(names(b.orderAdjustments())).anyMatch(n -> n.startsWith("Bulk order"));
		assertThat(b.orderAdjustments().get(0).amount()).isEqualByComparingTo("-240.00");
		b.lines().forEach(l -> assertThat(l.adjustments().get(0).amount()).isEqualByComparingTo("32.00"));
		assertThat(b.total()).isEqualByComparingTo("4828.80");
	}

	@Test
	void airBulkReduction_usesTotalUnitsOfTheOrder() {
		// 600 + 500 = 1100 units; each line alone would be <= 1000. Plastic, Other,
		// Air, 1.00 each.
		PriceBreakdown b = price(Destination.OTHER, ShippingMode.AIR, line(600, "1.00", PackageType.PLASTIC),
				line(500, "1.00", PackageType.PLASTIC));
		// goods 1100; bulk -220 (base 880); plastic +48.00 (480) and +40.00 (400);
		// other 15% of 880 = +132.00;
		// air 1100 x 30 = 33000; air bulk -15% = -4950.00
		assertThat(names(b.orderAdjustments())).anyMatch(n -> n.startsWith("Air shipping bulk"));
		assertThat(b.total()).isEqualByComparingTo("29150.00");
	}

	@Test
	void differentPackagesPerLine_areCostedPerLine() {
		// Red Small (plastic) 10 x 10.00 = 100; Yellow XLarge (wood) 20 x 20.00 = 400.
		// 30 units: no discount.
		PriceBreakdown b = price(Destination.OTHER, ShippingMode.LAND, line(10, "10.00", PackageType.PLASTIC),
				line(20, "20.00", PackageType.WOOD));
		assertThat(b.lines().get(0).adjustments().get(0).name()).startsWith("Plastic package");
		assertThat(b.lines().get(0).adjustments().get(0).amount()).isEqualByComparingTo("10.00");
		assertThat(b.lines().get(0).total()).isEqualByComparingTo("110.00");
		assertThat(b.lines().get(1).adjustments().get(0).name()).startsWith("Wood package");
		assertThat(b.lines().get(1).adjustments().get(0).amount()).isEqualByComparingTo("20.00");
		assertThat(b.lines().get(1).total()).isEqualByComparingTo("420.00");
		// other 15% of 500 = +75.00; land 30 units x 10 = +300.00 -> 500 + 10 + 20 + 75
		// + 300
		assertThat(b.total()).isEqualByComparingTo("905.00");
	}

	@Test
	void seaFee_isChargedOncePerOrder() {
		// 3 cardboard lines, 1 unit @10.00 each, Bolivia, Sea
		PriceBreakdown b = price(Destination.BOLIVIA, ShippingMode.SEA, line(1, "10.00", PackageType.CARDBOARD),
				line(1, "10.00", PackageType.CARDBOARD), line(1, "10.00", PackageType.CARDBOARD));
		assertThat(names(b.orderAdjustments()).stream().filter(n -> n.startsWith("Sea")).count()).isEqualTo(1);
		// 30.00 - 0.30 (cardboard -1% x 3) + 3.90 (Bolivia 13% of 30) + 400
		assertThat(b.total()).isEqualByComparingTo("433.60");
	}

	// ---------- general ----------

	@Test
	void destinationMapping() {
		assertThat(Destination.from("  usa ")).isEqualTo(Destination.USA);
		assertThat(Destination.from("Bolivia")).isEqualTo(Destination.BOLIVIA);
		assertThat(Destination.from("INDIA")).isEqualTo(Destination.INDIA);
		assertThat(Destination.from("Narnia")).isEqualTo(Destination.OTHER);
	}

	@Test
	void breakdownAlwaysSumsToTotal_inCents() {
		PriceBreakdown b = price(Destination.OTHER, ShippingMode.LAND, line(3, "0.33", PackageType.CARDBOARD),
				line(7, "1.17", PackageType.WOOD));
		BigDecimal sum = b.goodsCost();
		for (LineBreakdown l : b.lines()) {
			for (PriceAdjustment a : l.adjustments())
				sum = sum.add(a.amount());
		}
		for (PriceAdjustment a : b.orderAdjustments())
			sum = sum.add(a.amount());
		assertThat(b.total()).isEqualByComparingTo(sum);
		assertThat(b.total().scale()).isEqualTo(2);
		// and the line totals + order adjustments add up to the same number
		BigDecimal viaLines = b.lines().stream().map(LineBreakdown::total).reduce(BigDecimal.ZERO, BigDecimal::add);
		for (PriceAdjustment a : b.orderAdjustments())
			viaLines = viaLines.add(a.amount());
		assertThat(viaLines).isEqualByComparingTo(b.total());
	}

	@Test
	void calculatorIsReusableAcrossCalls() {
		PriceBreakdown first = price(Destination.USA, ShippingMode.LAND, line(10, "5.00", PackageType.WOOD));
		PriceBreakdown second = price(Destination.USA, ShippingMode.LAND, line(10, "5.00", PackageType.WOOD));
		assertThat(second.total()).isEqualByComparingTo(first.total());
		assertThat(second.orderAdjustments()).hasSameSizeAs(first.orderAdjustments());
	}
}

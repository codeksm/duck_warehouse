package com.duck.warehouse.warehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.duck.warehouse.shared.Color;
import com.duck.warehouse.shared.Size;
import com.duck.warehouse.warehouse.domain.Duck;
import com.duck.warehouse.warehouse.dto.AddDuckRequest;
import com.duck.warehouse.warehouse.repo.DuckRepository;
import com.duck.warehouse.warehouse.service.DuckService;

/**
 * Proves the merge invariant under concurrency against a real MongoDB.
 * Run with: MONGO_IT=true mvn test -Dtest=DuckMergeConcurrencyTest   (MongoDB must be up, see README)
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "MONGO_IT", matches = "true")
public class DuckMergeConcurrencyTest {
	@Autowired DuckService service;
    @Autowired DuckRepository repository;

    @Test
    void concurrentAddsOfSameDuckProduceOneRecordWithSummedQuantity() throws Exception {
        repository.deleteAll();
        int threads = 32;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return service.add(new AddDuckRequest("Red", "Large", 5.00, 3));
            }));
        }
        start.countDown();
        for (Future<?> f : futures) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        List<Duck> live = repository.findAll().stream().filter(d -> !d.deleted()).toList();
        assertThat(live).hasSize(1);
        assertThat(live.get(0).color()).isEqualTo(Color.RED);
        assertThat(live.get(0).size()).isEqualTo(Size.LARGE);
        assertThat(live.get(0).quantity()).isEqualTo(threads * 3);
    }
}

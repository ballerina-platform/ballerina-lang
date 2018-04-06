package org.ballerinalang.observe.metrics.extension.micrometer;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.ballerinalang.util.metrics.CallbackGauge;
import org.ballerinalang.util.metrics.Counter;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.MetricRegistry;
import org.ballerinalang.util.metrics.Summary;
import org.ballerinalang.util.metrics.Timer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class MicrometerMetricProviderTest {

    private MetricRegistry metricRegistry;

    @BeforeClass
    public void init() {
        metricRegistry = new MetricRegistry(new MicrometerMetricProvider(new SimpleMeterRegistry()));
    }

    @Test
    public void testCounter() {
        Counter counter = Counter.builder("test_counter").description("Test Counter").register(metricRegistry);
        counter.increment(100D);
        Assert.assertEquals(counter.count(), 100D);
    }

    @Test
    public void testCounterTags() {
        Counter counter = Counter.builder("test_tags_counter").description("Test Counter Tags")
                .tags("key", "value").register(metricRegistry);
        counter.increment(100D);
        Assert.assertEquals(counter.count(), 100D);
    }

    @Test
    public void testCounterTagsFailures() {
        try {
            Counter.builder("test_tag_fail_counter").description("Test Counter Tags Failures")
                    .tags("key").register(metricRegistry);
            Assert.fail("Different number of tag values are not allowed");
        } catch (IllegalArgumentException e) {
            // Test successful
        }
    }

    @Test
    public void testGauge() {
        Gauge gauge = Gauge.builder("test_gauge").description("Test Gauge").register(metricRegistry);
        gauge.increment(100D);
        gauge.decrement(50D);
        Assert.assertEquals(gauge.get(), 50D);
    }

    @Test
    public void testCallbackGauge() {
        List<Integer> list = new ArrayList();
        IntStream.range(0, 100).forEach(list::add);
        CallbackGauge gauge = CallbackGauge.builder("test_callback_gauge", list, List::size).description("")
                .register(metricRegistry);
        Assert.assertEquals(gauge.get(), 100D);
    }

    @Test
    public void testSummary() {
        Summary summary = Summary.builder("test_summary").description("Test Summary").register(metricRegistry);
        summary.record(100);
        Assert.assertEquals(summary.count(), 1);
    }

    @Test
    public void testTimer() {
        Timer timer = Timer.builder("test_timer").description("Test Timer").tag("method", "record")
                .register(metricRegistry);
        timer.record(100, TimeUnit.NANOSECONDS);
        Assert.assertEquals(timer.count(), 1);
    }

}

/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.test.observability;

import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.observability.tracer.BSpan;
import io.ballerina.runtime.observability.tracer.TracersStore;
import io.ballerina.runtime.observability.tracer.spi.TracerProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests adding span links through {@link BSpan} and {@link ObserveUtils}.
 *
 * @since 2201.14.1
 */
public class SpanLinkTest {

    private static final String SERVICE = "span-link-test";
    private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
    private SdkTracerProvider sdkTracerProvider;

    @BeforeClass
    public void setup() {
        sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build();
        TracersStore.getInstance().setTracerGenerator(new TracerProvider() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public void init() {
            }

            @Override
            public Tracer getTracer(String serviceName) {
                return sdkTracerProvider.get(serviceName);
            }

            @Override
            public ContextPropagators getPropagators() {
                return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
            }
        });
    }

    @AfterMethod
    public void reset() {
        exporter.reset();
    }

    @Test
    public void testLinkWithTypedAttributes() {
        SpanContext producer = startProducerSpan();
        BSpan consumer = BSpan.start(SERVICE, "poll", false);
        consumer.addLink(headersOf(producer), Attributes.of(
                AttributeKey.longKey("messaging.kafka.offset"), 42L,
                AttributeKey.stringKey("messaging.destination.partition.id"), "3"));
        consumer.finishSpan();

        LinkData link = singleLink(consumerSpan());
        Assert.assertEquals(link.getSpanContext().getTraceId(), producer.getTraceId());
        Assert.assertEquals(link.getSpanContext().getSpanId(), producer.getSpanId());
        Assert.assertEquals(link.getAttributes().get(AttributeKey.longKey("messaging.kafka.offset")), Long.valueOf(42));
        Assert.assertEquals(link.getAttributes().get(AttributeKey.stringKey("messaging.destination.partition.id")),
                "3");
    }

    @Test
    public void testLinkWithStringAttributes() {
        SpanContext producer = startProducerSpan();
        BSpan consumer = BSpan.start(SERVICE, "poll", false);
        consumer.addLink(headersOf(producer), Map.of("messaging.destination.partition.id", "3"));
        consumer.finishSpan();

        LinkData link = singleLink(consumerSpan());
        Assert.assertEquals(link.getSpanContext().getSpanId(), producer.getSpanId());
        Assert.assertEquals(link.getAttributes().get(AttributeKey.stringKey("messaging.destination.partition.id")),
                "3");
    }

    @Test
    public void testLinkMatchesHeaderNamesCaseInsensitively() {
        SpanContext producer = startProducerSpan();
        Map<String, String> headers = new HashMap<>();
        headers.put("Traceparent", headersOf(producer).get("traceparent"));
        BSpan consumer = BSpan.start(SERVICE, "poll", false);
        consumer.addLink(headers);
        consumer.finishSpan();

        Assert.assertEquals(singleLink(consumerSpan()).getSpanContext().getSpanId(), producer.getSpanId());
    }

    @Test
    public void testNoLinkForMissingOrInvalidContext() {
        BSpan consumer = BSpan.start(SERVICE, "poll", false);
        consumer.addLink(null);
        consumer.addLink(Map.of());
        consumer.addLink(Map.of("traceparent", "not-a-trace-context"));
        consumer.finishSpan();

        Assert.assertTrue(consumerSpan().getLinks().isEmpty());
    }

    @Test
    public void testInvalidContextDoesNotLinkToCurrentSpan() {
        Span current = sdkTracerProvider.get(SERVICE).spanBuilder("current").startSpan();
        BSpan consumer = BSpan.start(SERVICE, "poll", false);
        try (Scope ignored = current.makeCurrent()) {
            consumer.addLink(Map.of("traceparent", "not-a-trace-context"));
        }
        consumer.finishSpan();
        current.end();

        Assert.assertTrue(consumerSpan().getLinks().isEmpty());
    }

    @Test
    public void testLinkAfterSpanEndedIsIgnored() {
        SpanContext producer = startProducerSpan();
        BSpan consumer = BSpan.start(SERVICE, "poll", false);
        consumer.finishSpan();
        consumer.addLink(headersOf(producer));

        Assert.assertTrue(consumerSpan().getLinks().isEmpty());
    }

    @Test
    public void testAddLinkToSpanWithoutContext() {
        ObserveUtils.addLinkToSpan(null, headersOf(startProducerSpan()));
        ObserveUtils.addLinkToSpan(new ObserverContext(), headersOf(startProducerSpan()));
    }

    @Test
    public void testAddLinkToSpanWhenObservabilityDisabled() {
        if (ObserveUtils.isObservabilityEnabled()) {
            throw new SkipException("observability is enabled in this run");
        }
        SpanContext producer = startProducerSpan();
        BSpan consumer = BSpan.start(SERVICE, "poll", false);
        ObserverContext observerContext = new ObserverContext();
        observerContext.setSpan(consumer);
        ObserveUtils.addLinkToSpan(observerContext, headersOf(producer));
        consumer.finishSpan();

        Assert.assertTrue(consumerSpan().getLinks().isEmpty());
    }

    private SpanContext startProducerSpan() {
        Span producer = sdkTracerProvider.get(SERVICE).spanBuilder("send").startSpan();
        producer.end();
        return producer.getSpanContext();
    }

    private static Map<String, String> headersOf(SpanContext spanContext) {
        Map<String, String> headers = new HashMap<>();
        W3CTraceContextPropagator.getInstance().inject(Context.root().with(Span.wrap(spanContext)), headers,
                Map::put);
        return headers;
    }

    private SpanData consumerSpan() {
        List<SpanData> spans = exporter.getFinishedSpanItems().stream()
                .filter(span -> span.getName().equals("poll"))
                .toList();
        Assert.assertEquals(spans.size(), 1);
        return spans.get(0);
    }

    private static LinkData singleLink(SpanData span) {
        Assert.assertEquals(span.getLinks().size(), 1);
        return span.getLinks().get(0);
    }
}

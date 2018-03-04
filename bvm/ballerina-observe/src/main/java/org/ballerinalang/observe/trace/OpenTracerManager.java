package org.ballerinalang.observe.trace;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import org.ballerinalang.observe.trace.config.ConfigLoader;
import org.ballerinalang.observe.trace.config.OpenTracingConfig;
import org.ballerinalang.observe.trace.exception.UnknownSpanContextTypeException;
import org.ballerinalang.util.tracer.TraceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the class which holds the tracers that are enabled, and bridges all tracers with instrumented code.
 */
public class OpenTracerManager implements TraceManager {

    private static OpenTracerManager instance = new OpenTracerManager();
    private TracersStore tracerStore;

    public OpenTracerManager() {
        OpenTracingConfig openTracingConfig = ConfigLoader.load();
        if (openTracingConfig != null) {
            tracerStore = new TracersStore(openTracingConfig);
        }
    }

    public static OpenTracerManager getInstance() {
        return instance;
    }

    /**
     * Method to extract span context from a carrier.
     *
     * @param format      {@code (Format<TextMap>)} format in which the span context is received.
     * @param httpHeaders the properties map extracted and used to create span context.
     * @param serviceName to retrieve the relevant tracer instance.
     * @return the span context which includes tracer specific spans.
     */
    @Override
    public Map<String, Object> extract(Object format, Map<String, String> httpHeaders, String serviceName) {
        Map<String, Object> spanContext = new HashMap<>();
        if (format == null) {
            format = Format.Builtin.HTTP_HEADERS;
        }

        Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
        for (Map.Entry<String, Tracer> tracerEntry : tracers.entrySet()) {
            spanContext.put(tracerEntry.getKey(),
                    tracerEntry.getValue().extract((Format<TextMap>) format,
                            new RequestExtractor(httpHeaders.entrySet().iterator())));
        }
        return spanContext;
    }

    /**
     * Method to inject a span context to a carrier.
     *
     * @param activeSpanMap the spans to be injected to the carrier.
     * @param format        the format {@code (Format<TextMap>)} in which the span context will be injected to the
     *                      carrier.
     * @param serviceName   to retrieve the relevant tracer instance.
     * @return the carrier with the injected the span context.
     */
    @Override
    public Map<String, String> inject(Map<String, Object> activeSpanMap, Object format, String serviceName) {
        HashMap<String, String> carrierMap = new HashMap<>();
        if (format == null) {
            format = Format.Builtin.HTTP_HEADERS;
        }
        for (Map.Entry<String, Object> activeSpanEntry : activeSpanMap.entrySet()) {
            Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
            Tracer tracer = tracers.get(activeSpanEntry.getKey());
            if (tracer != null) {
                Span span = (Span) activeSpanEntry.getValue();
                if (span != null) {
                    tracer.inject(span.context(), (Format<TextMap>) format, new RequestInjector(carrierMap));
                }
            }
        }
        return carrierMap;
    }

    @Override
    public Map<String, Object> buildSpan(long invocationId, String spanName, Map<String, Object> spanContextMap,
                                         Map<String, String> tags, boolean makeActive, String serviceName) {
        Map<String, Object> spanMap = new HashMap<>();
        Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
        for (Map.Entry spanContextEntry : spanContextMap.entrySet()) {
            Tracer tracer = tracers.get(spanContextEntry.getKey().toString());
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName);
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                spanBuilder = spanBuilder.withTag(tag.getKey(), tag.getValue());
            }
            spanBuilder.withTag(Constants.INVOCATION_ID_PROPERTY, invocationId);
            if (spanContextEntry.getValue() != null) {
                if (spanContextEntry.getValue() instanceof SpanContext) {
                    spanBuilder = spanBuilder.asChildOf((SpanContext) spanContextEntry.getValue());
                } else if (spanContextEntry.getValue() instanceof Span) {
                    spanBuilder = spanBuilder.asChildOf((Span) spanContextEntry.getValue());
                } else {
                    throw new UnknownSpanContextTypeException("Unknown span context field - " +
                            spanContextEntry.getValue().getClass()
                            + "! Open tracing can span can be build only by using "
                            + SpanContext.class + " or " + Span.class);
                }
            }
            Span span = spanBuilder.start();
            span.setBaggageItem(Constants.INVOCATION_ID_PROPERTY, String.valueOf(invocationId));
            if (makeActive) {
                tracer.scopeManager().activate(span, false);
            }
            spanMap.put(spanContextEntry.getKey().toString(), span);
        }
        return spanMap;
    }

    @Override
    public void finishSpan(List<Object> spans) {
        for (Object spanObj : spans) {
            Span span = (Span) spanObj;
            span.finish();
        }
    }

    @Override
    public void log(List<Object> spanList, Map<String, Object> fields) {
        for (Object spanObj : spanList) {
            Span span = (Span) spanObj;
            span.log(fields);
        }
    }

    @Override
    public void addTags(List<Object> spanList, Map<String, String> tags) {
        for (Object spanObj : spanList) {
            Span span = (Span) spanObj;
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                span.setTag(tag.getKey(), String.valueOf(tag.getValue()));
            }
        }
    }
}

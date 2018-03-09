package org.ballerinalang.observe.trace;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import org.ballerinalang.observe.trace.config.ConfigLoader;
import org.ballerinalang.observe.trace.config.OpenTracingConfig;
import org.ballerinalang.util.tracer.TraceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the class which holds the tracers that are enabled,
 * and bridges all tracers with instrumented code. This also helps
 * decouple tracing from ballerina core.
 * Note: This class will be loaded by {@code TraceManagerWrapper}.
 */
public class OpenTracerManager implements TraceManager {
    private TracersStore tracerStore;
    private boolean enabled = false;

    public OpenTracerManager() {
        OpenTracingConfig openTracingConfig = ConfigLoader.load();
        if (openTracingConfig != null) {
            tracerStore = new TracersStore(openTracingConfig);
            enabled = true;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Map<String, Object> extract(Object format, Map<String, String> headers, String serviceName) {
        Map<String, Object> spanContext = new HashMap<>();
        if (format == null) {
            format = Format.Builtin.HTTP_HEADERS;
        }
        Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
        for (Map.Entry<String, Tracer> tracerEntry : tracers.entrySet()) {
            spanContext.put(tracerEntry.getKey(),
                    tracerEntry.getValue().extract((Format<TextMap>) format,
                            new RequestExtractor(headers)));
        }
        return spanContext;
    }

    @Override
    public Map<String, String> inject(Map<String, ?> activeSpanMap, Object format, String serviceName) {
        HashMap<String, String> carrierMap = new HashMap<>();
        if (format == null) {
            format = Format.Builtin.HTTP_HEADERS;
        }
        for (Map.Entry<String, ?> activeSpanEntry : activeSpanMap.entrySet()) {
            Map<String, Tracer> tracers = tracerStore.getTracers(serviceName);
            Tracer tracer = tracers.get(activeSpanEntry.getKey());
            if (tracer != null && activeSpanEntry.getValue() instanceof Span) {
                Span span = (Span) activeSpanEntry.getValue();
                if (span != null) {
                    tracer.inject(span.context(), (Format<TextMap>) format,
                            new RequestInjector(carrierMap));
                }
            }
        }
        return carrierMap;
    }

    @Override
    public Map<String, Object> startSpan(long invocationId, String spanName, Map<String, ?> spanContextMap,
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
    public void finishSpan(List<?> spans) {
        for (Object spanObj : spans) {
            if (spanObj instanceof Span) {
                Span span = (Span) spanObj;
                span.finish();
            }
        }
    }

    @Override
    public void log(List<?> spans, Map<String, ?> fields) {
        for (Object spanObj : spans) {
            if (spanObj instanceof Span) {
                Span span = (Span) spanObj;
                span.log(fields);
            }
        }
    }

    @Override
    public void addTags(List<?> spanList, Map<String, String> tags) {
        for (Object spanObj : spanList) {
            if (spanObj instanceof Span) {
                Span span = (Span) spanObj;
                for (Map.Entry<String, String> tag : tags.entrySet()) {
                    span.setTag(tag.getKey(), String.valueOf(tag.getValue()));
                }
            }
        }
    }
}

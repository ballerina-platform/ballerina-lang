module io.ballerina.observability {
    requires io.ballerina.lang;
    requires io.ballerina.runtime;
    requires io.opentelemetry.api;
    requires io.opentelemetry.context;

    uses io.ballerina.runtime.observability.metrics.spi.MetricProvider;
    uses io.ballerina.runtime.observability.tracer.spi.TracerProvider;
}

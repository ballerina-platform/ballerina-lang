module io.ballerina.observability {
    requires io.ballerina.lang;
    requires io.ballerina.runtime;
    requires opentracing.api;
    requires opentracing.noop;
    requires io.ballerina.config;

    uses io.ballerina.runtime.observability.metrics.spi.MetricProvider;
    uses io.ballerina.runtime.observability.tracer.spi.TracerProvider;
}

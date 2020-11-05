module io.ballerina.observe.trace.extension.jaeger {
    requires io.ballerina.runtime.internal;
    requires io.ballerina.config;
    requires opentracing.api;
    requires jaeger.core;

    exports org.ballerinalang.observe.trace.extension.jaeger;
}

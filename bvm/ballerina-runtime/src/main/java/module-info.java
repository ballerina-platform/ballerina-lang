module io.ballerina.runtime {
    requires java.xml;
    requires woodstox.core.asl;
    requires org.apache.commons.lang3;
    requires axiom.api;
    requires java.logging;
    requires java.management;
    requires opentracing.api;
    requires java.transaction.xa;
    requires slf4j.api;
    requires axiom.impl;
    requires io.ballerina.config;
    requires io.ballerina.logging;

    requires transactions.jta;
    requires java.transaction;
    requires java.naming;

    exports io.ballerina.runtime.api;
    exports io.ballerina.runtime.api.async;
    exports io.ballerina.runtime.api.values;
    exports io.ballerina.runtime.api.types;

    exports io.ballerina.runtime.util;
    exports io.ballerina.runtime.util.exceptions;
    exports io.ballerina.runtime.launch;
    exports io.ballerina.runtime;
    exports io.ballerina.runtime.scheduling;
    exports io.ballerina.runtime.observability;
    exports io.ballerina.runtime.transactions;
    exports io.ballerina.runtime.services;
    exports io.ballerina.runtime.observability.metrics;
    exports io.ballerina.runtime.commons;
    exports io.ballerina.runtime.observability.tracer;
    exports io.ballerina.runtime.observability.metrics.spi;
    exports io.ballerina.runtime.services.spi;
    exports io.ballerina.runtime.annotation;
    exports io.ballerina.runtime.values;
    exports io.ballerina.runtime.types;
}

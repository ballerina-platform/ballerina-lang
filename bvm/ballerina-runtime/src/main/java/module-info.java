module io.ballerina.jvm {
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

    exports io.ballerina.jvm.types;
    exports io.ballerina.jvm.util;
    exports io.ballerina.jvm.api;
    exports io.ballerina.jvm.api.connector;
    exports io.ballerina.jvm.api.values;
    exports io.ballerina.jvm.internal;
    exports io.ballerina.jvm.util.exceptions;
    exports io.ballerina.jvm.launch;
    exports io.ballerina.jvm;
    exports io.ballerina.jvm.values.connector;
    exports io.ballerina.jvm.scheduling;
    exports io.ballerina.jvm.observability;
    exports io.ballerina.jvm.runtime;
    exports io.ballerina.jvm.transactions;
    exports io.ballerina.jvm.services;
    exports io.ballerina.jvm.observability.metrics;
    exports io.ballerina.jvm.commons;
    exports io.ballerina.jvm.observability.tracer;
    exports io.ballerina.jvm.observability.metrics.spi;
    exports io.ballerina.jvm.services.spi;
    exports io.ballerina.jvm.annotation;
    exports io.ballerina.jvm.api.types;
    exports io.ballerina.jvm.api.runtime;
}

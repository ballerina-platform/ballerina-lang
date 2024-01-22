module io.ballerina.runtime {
    requires java.xml;
    requires org.apache.commons.text;
    requires axiom.api;
    requires java.logging;
    requires io.opentelemetry.api;
    requires io.opentelemetry.context;
    requires java.transaction.xa;
    requires org.slf4j;
    requires axiom.impl;
    requires io.ballerina.toml;
    requires io.ballerina.tools.api;

    requires transactions.jta;
    requires java.transaction;
    requires org.apache.commons.lang3;
    requires io.ballerina.identifier;
    requires jdk.unsupported;

    // API exports
    exports io.ballerina.runtime.api;
    exports io.ballerina.runtime.api.async;
    exports io.ballerina.runtime.api.constants;
    exports io.ballerina.runtime.api.creators;
    exports io.ballerina.runtime.api.flags;
    exports io.ballerina.runtime.api.launch;
    exports io.ballerina.runtime.api.types;
    exports io.ballerina.runtime.api.utils;
    exports io.ballerina.runtime.api.values;

    exports io.ballerina.runtime.observability;
    exports io.ballerina.runtime.observability.metrics;
    exports io.ballerina.runtime.observability.metrics.spi;
    exports io.ballerina.runtime.observability.tracer;
    exports io.ballerina.runtime.observability.tracer.spi;
    exports io.ballerina.runtime.transactions;

    // export only for Langlib , Cli and Testerina
    exports io.ballerina.runtime.internal.errors to io.ballerina.lang.value, io.ballerina.lang.integer,
            io.ballerina.java, io.ballerina.lang.internal, io.ballerina.lang.array, io.ballerina.lang.bool,
            io.ballerina.lang.floatingpoint, io.ballerina.lang.map, io.ballerina.lang.string, io.ballerina.lang.table,
            io.ballerina.lang.xml, io.ballerina.testerina.core, io.ballerina.cli.utils, io.ballerina.cli,
            io.ballerina.lang.decimal, org.ballerinalang.debugadapter.runtime, io.ballerina.lang.function,
            io.ballerina.lang.regexp;
    exports io.ballerina.runtime.internal.values to org.ballerinalang.debugadapter.runtime, io.ballerina.lang.query,
            io.ballerina.lang.function, io.ballerina.lang.regexp, io.ballerina.lang.value;
    exports io.ballerina.runtime.internal.configurable.providers.toml to org.ballerinalang.debugadapter.runtime;
    exports io.ballerina.runtime.internal.types to io.ballerina.lang.typedesc, io.ballerina.testerina.runtime,
            org.ballerinalang.debugadapter.runtime, io.ballerina.lang.function, io.ballerina.lang.regexp, io.ballerina.testerina.core;
    exports io.ballerina.runtime.observability.metrics.noop;
    exports io.ballerina.runtime.observability.tracer.noop;
    exports io.ballerina.runtime.internal.regexp;
}

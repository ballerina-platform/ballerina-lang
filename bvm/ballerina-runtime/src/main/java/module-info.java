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
    exports io.ballerina.runtime.transactions;

    // export only for Langlib , Cli and Testerina
    exports io.ballerina.runtime.internal to io.ballerina.testerina.core, io.ballerina.testerina.runtime,
            io.ballerina.lang, io.ballerina.lang.map, io.ballerina.lang.test, io.ballerina.lang.array,
            io.ballerina.lang.table, io.ballerina.lang.value, io.ballerina.lang.xml, ballerina.debug.adapter.core, io.ballerina.tool;
    exports io.ballerina.runtime.internal.commons to io.ballerina.lang.value;
    exports io.ballerina.runtime.internal.launch to io.ballerina.testerina.runtime, io.ballerina.packerina,
            ballerina.test.listener, io.ballerina.cli;
    exports io.ballerina.runtime.internal.scheduling to
            io.ballerina.lang.array, io.ballerina.testerina.runtime, io.ballerina.lang.error, io.ballerina.java,
            io.ballerina.lang.map, io.ballerina.lang.table, io.ballerina.lang.value, io.ballerina.lang.xml,
            io.ballerina.lang.transaction, io.ballerina.log.api, io.ballerina.testerina.core,
            io.ballerina.cli.utils, io.ballerina.lang.internal;
    exports io.ballerina.runtime.internal.util to io.ballerina.testerina.runtime, io.ballerina.lang, io.ballerina.tool,
            io.ballerina.lang.integer, io.ballerina.lang.floatingpoint, io.ballerina.lang.array,
            io.ballerina.lang.table, io.ballerina.java, io.ballerina.lang.map, io.ballerina.lang.string,
            io.ballerina.lang.xml, io.ballerina.lang.bool, io.ballerina.lang.error, io.ballerina.lang.internal,
            io.ballerina.auth, io.ballerina.runtime.api, io.ballerina.cli.utils;
    exports io.ballerina.runtime.internal.util.exceptions to io.ballerina.lang.value, io.ballerina.lang.integer,
            io.ballerina.java, io.ballerina.lang.internal, io.ballerina.lang.array, io.ballerina.lang.bool,
            io.ballerina.lang.floatingpoint, io.ballerina.lang.map, io.ballerina.lang.string, io.ballerina.lang.table,
            io.ballerina.lang.xml, io.ballerina.testerina.core, io.ballerina.cli.utils;
    exports io.ballerina.runtime.internal.values to io.ballerina.testerina.core, io.ballerina.testerina.runtime;
}

module io.ballerina.cli {
    exports io.ballerina.cli;
    exports io.ballerina.cli.launcher;

    requires io.ballerina.runtime;
    requires io.ballerina.lang;
    requires io.ballerina.tools.api;
    requires io.ballerina.testerina.runtime;
    requires io.ballerina.testerina.core;
    requires info.picocli;
    requires org.apache.commons.compress;
    requires gson;
    requires org.jacoco.core;
    requires io.ballerina.central.client;
    requires io.ballerina.docerina;
    requires jdk.httpserver;
    requires io.ballerina.config;
    requires io.ballerina.core;
    requires slf4j.api;
    requires io.ballerina.shell.cli;
}

module io.ballerina.cli {
    exports io.ballerina.cli;
    exports io.ballerina.cli.launcher;
    exports io.ballerina.cli.launcher.utils;

    requires io.ballerina.runtime;
    requires io.ballerina.lang;
    requires io.ballerina.tools.api;
    requires io.ballerina.testerina.runtime;
    requires info.picocli;
    requires org.apache.commons.compress;
    requires gson;
    requires org.jacoco.core;
    requires io.ballerina.central.client;
    requires io.ballerina.docerina;
    requires jdk.httpserver;
    requires slf4j.api;
    requires io.ballerina.config;
    requires io.ballerina.core;
    requires testng;
}

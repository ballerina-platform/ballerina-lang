module io.ballerina.packerina {
    exports org.ballerinalang.packerina;
    requires io.ballerina.lang;
    requires io.ballerina.cli.module;
    requires io.ballerina.tool;
    requires io.ballerina.tools.api;
    requires io.ballerina.runtime;
    requires toml4j;
    requires info.picocli;
    requires io.ballerina.docerina;
    requires org.apache.commons.compress;
    requires io.ballerina.config;
    requires io.ballerina.testerina.runtime;
    requires io.ballerina.testerina.core;
    requires gson;
}
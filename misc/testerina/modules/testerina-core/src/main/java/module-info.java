module io.ballerina.testerina.core {
    exports org.ballerinalang.testerina.core;
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires io.ballerina.testerina.runtime;
    requires io.ballerina.runtime;
    requires io.github.javadiffutils;
    requires com.google.gson;
}

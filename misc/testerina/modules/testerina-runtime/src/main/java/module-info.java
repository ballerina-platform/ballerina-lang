module io.ballerina.testerina.runtime {
    exports org.ballerinalang.test.runtime.entity;
    exports org.ballerinalang.test.runtime.util;
    exports org.ballerinalang.test.runtime;
    requires io.ballerina.runtime;
    requires com.google.gson;
    requires org.jacoco.core;
    requires io.ballerina.lang;
    requires io.ballerina.tools.api;
    requires org.jacoco.report;
    requires io.ballerina.identifier;
}

module io.ballerina.testerina.runtime {
    exports org.ballerinalang.test.runtime.entity;
    exports org.ballerinalang.test.runtime.util;
    requires io.ballerina.runtime;
    requires gson;
    requires org.jacoco.core;
}
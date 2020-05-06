module ballerina.runtime {
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
    requires ballerina.config;
    requires ballerina.logging;
    exports org.ballerinalang.jvm.values;
    exports org.ballerinalang.jvm.types;
    exports org.ballerinalang.jvm.util;
    exports org.ballerinalang.jvm.values.api;
    exports org.ballerinalang.jvm.util.exceptions;
    exports org.ballerinalang.jvm.values.utils;
    exports org.ballerinalang.jvm.launch;
    exports org.ballerinalang.jvm;
}

module io.ballerina.core {
    requires io.ballerina.lang;
    requires axiom.api;
    requires axiom.impl;
    requires org.apache.commons.lang3;
    requires java.xml;
    requires axiom.c14n;
    exports org.ballerinalang.core.model;
    exports org.ballerinalang.core.util.exceptions;
    exports org.ballerinalang.core.model.types;
    exports org.ballerinalang.core.model.values;
    exports org.ballerinalang.core.model.util;
    exports org.ballerinalang.bre;
    exports org.ballerinalang.bre.bvm;
}
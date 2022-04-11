module io.ballerina.docerina {
    exports org.ballerinalang.docgen.docs;
    exports org.ballerinalang.docgen;
    exports org.ballerinalang.docgen.generator.model;
    requires io.ballerina.lang;
    requires com.google.gson;
    requires org.apache.commons.io;
    requires slf4j.api;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
}
module io.ballerina.docerina {
    exports org.ballerinalang.docgen.docs;
    exports org.ballerinalang.docgen;
    exports org.ballerinalang.docgen.generator.model;
    requires io.ballerina.lang;
    requires com.google.gson;
    requires org.apache.commons.io;
    requires org.slf4j;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires okhttp3;
    requires static org.jetbrains.annotations;
}

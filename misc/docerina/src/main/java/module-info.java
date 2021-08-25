module io.ballerina.docerina {
    exports org.ballerinalang.docgen.docs;
    requires org.apache.commons.lang3;
    exports org.ballerinalang.docgen.generator.model;
    exports org.ballerinalang.docgen;
    requires io.ballerina.lang;
    requires io.ballerina.config;
    requires com.google.gson;
    requires org.apache.commons.io;
    requires slf4j.api;
    requires org.commonmark;
    requires org.commonmark.ext.gfm.tables;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
}

module io.ballerina.docerina {
    exports org.ballerinalang.docgen.docs;
    exports org.ballerinalang.docgen.model;
    requires org.apache.commons.lang3;
    requires io.ballerina.lang;
    requires handlebars;
    requires io.ballerina.config;
    requires gson;
    requires org.apache.commons.io;
    requires slf4j.api;
    requires org.commonmark;
    requires org.commonmark.ext.gfm.tables;
}
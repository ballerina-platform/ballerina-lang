module io.ballerina.openapi.generator {
    exports org.ballerinalang.openapi;
    exports org.ballerinalang.openapi.exception;
    exports org.ballerinalang.openapi.model;
    exports org.ballerinalang.openapi.utils;
    exports org.ballerinalang.openapi.typemodel;
    requires handlebars;
    requires io.swagger.v3.oas.models;
    requires swagger.parser.v3;
    requires org.apache.commons.lang3;
    requires io.ballerina.tool;
    requires io.ballerina.lang;
    requires info.picocli;
    requires io.ballerina.openapi.convertor;
    requires java.ws.rs;
    requires jsr305;

}
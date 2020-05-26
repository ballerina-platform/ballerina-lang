module io.ballerina.openapi.convertor {
    exports org.ballerinalang.ballerina.openapi.convertor.service;
    exports org.ballerinalang.ballerina.openapi.convertor;
    requires io.ballerina.lang;
    requires swagger.models;
    requires swagger.parser;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires swagger.parser.v3;
    requires swagger.parser.v2.converter;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires io.ballerina.tool;
    requires com.google.common;
    requires io.ballerina.http;
    requires java.ws.rs;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires swagger.core;
    requires org.slf4j;
    requires com.fasterxml.jackson.annotation;
    requires io.ballerina.language.server.compiler;
}
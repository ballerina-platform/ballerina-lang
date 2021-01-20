import ballerina/jballerina.java;
import ballerina/jballerina.java;

function systemOut() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

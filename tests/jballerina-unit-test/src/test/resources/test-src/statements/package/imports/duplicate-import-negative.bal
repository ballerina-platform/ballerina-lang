import ballerina/java;
import ballerina/java;

function systemOut() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

import ballerina/java;

function splitExternal(handle receiver, string delimeter) returns handle = @java:Method {
    name: "split",
    'class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

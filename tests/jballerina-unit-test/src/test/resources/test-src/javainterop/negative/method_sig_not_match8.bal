import ballerina/java;

function splitExternal(handle receiver, string delimeter, boolean extraParam) returns handle = @java:Method {
    name: "split",
    'class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

import ballerina/jballerina.java;

public function foo() {
}

function getUUID() returns handle = @java:Method {
    name: "randomUUI",
    'class: "java.util.UUID"
} external;

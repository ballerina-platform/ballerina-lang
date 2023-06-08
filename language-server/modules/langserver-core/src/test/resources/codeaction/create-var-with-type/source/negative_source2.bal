import ballerina/jballerina.java;

function find(string location, TargetType targetType) returns targetType|CustomError = @java:Method {
    'class: "a.c.c.Find"
} external;

function negativeTest() {
    find("home");
}

public type TargetType typedesc<string|int>;

public type CustomError error<map<anydata>>;

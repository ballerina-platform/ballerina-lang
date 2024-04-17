import multimod.submod;
import ballerina/jballerina.java;
import ballerina/lang.runtime;

function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(String str) {
    println(system_out(), java:fromString(str));
}

public function main() {
    println(system_out(), java:fromString(submod:hello("World")));
    runtime:sleep(2000);
}

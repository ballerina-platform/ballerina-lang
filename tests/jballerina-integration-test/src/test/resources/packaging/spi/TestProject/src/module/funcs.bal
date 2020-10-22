import ballerina/java;

public function main() {
    string result = <string>java:toString(getDriversAsString());
    print(result);
}

function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}

public function getDriversAsString() returns handle = @java:Method {
      'class:"PrintDrivers"
} external;

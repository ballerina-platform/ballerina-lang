import ballerina/jballerina.java;

public function main() {
}

function builderWithStudentList(handle list, int index) returns handle = @java:Constructor {
    'class: "a.b.c.Builder",
    paramTypes: [{'class: "a.b.c.Student", dimensions:2}, "int"]
} external;

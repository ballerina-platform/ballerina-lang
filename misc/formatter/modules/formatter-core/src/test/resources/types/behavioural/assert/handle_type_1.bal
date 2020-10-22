import ballerina/java;

function newArrayDeque() returns handle = @java:Constructor {'class: "java.util.ArrayDeque"} external;

function offer(handle receiver, handle e) returns boolean = @java:Method {
    'class: "java.util.ArrayDeque",
    paramTypes: ["short"]
} external;

function poll(handle receiver) returns handle = @java:Method {'class: "java.util.ArrayDeque"} external;

public function foo() {
    var arrayDeque = newArrayDeque();
    handle nextInLineHandle = poll(arrayDeque);
}

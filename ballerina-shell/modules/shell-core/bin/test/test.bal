function outStream() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function print(handle printStream, any|error obj) = @java:Method {
    name: "print",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.Object"]
} external;

function println(any|error... objs) {
    handle outStreamObj = outStream();
    foreach var obj in objs {
        print(outStreamObj, obj);
    }
    print(outStreamObj, "\n");
}

function sprintfh(handle str, any|error... objs) returns handle = @java:Method {
    name: "format",
    'class: "java.lang.String"
} external;

function sprintf(string template, any|error... values) returns string {
    handle out = sprintfh(java:fromString(template), ...values);
    return java:toString(out) ?: "";
}

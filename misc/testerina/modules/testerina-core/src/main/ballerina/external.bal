import ballerina/jballerina.java;

isolated function print(handle printStream, any|error obj) = @java:Method {
    name: "print",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.Object"]
} external;

isolated function println(any|error... objs) {
    handle outStreamObj = outStream();
    foreach var obj in objs {
        print(outStreamObj, obj);
    }
    print(outStreamObj, "\n");
}

isolated function outStream() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

public isolated function split(string receiver, string delimiter) returns string[] {
    handle res = splitExternal(java:fromString(receiver), java:fromString(delimiter));
    return getBallerinaStringArray(res);
}

isolated function splitExternal(handle receiver, handle delimiter) returns handle = @java:Method {
    name: "split",
    'class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

isolated function getBallerinaStringArray(handle h) returns string[] = @java:Method {
    'class:"io.ballerina.runtime.api.utils.StringUtils",
    name:"fromStringArray",
    paramTypes:["[Ljava.lang.String;"]
} external;

isolated function sprintf(string format, (any|error)... args) returns string = @java:Method {
    name : "sprintf",
    'class : "org.ballerinalang.testerina.natives.io.Sprintf"
} external;

isolated function getBallerinaType((any|error) value) returns string = @java:Method {
    name : "getBallerinaType",
    'class : "org.ballerinalang.testerina.core.BallerinaTypeCheck"
} external;

isolated function getStringDiff(string actual, string expected) returns string = @java:Method {
     name : "getStringDiff",
     'class : "org.ballerinalang.testerina.core.AssertionDiffEvaluator"
 } external;


isolated function getKeysDiff(string[] actualKeys, string[] expectedKeys) returns string = @java:Method {
    name: "getKeysDiff",
    'class: "org.ballerinalang.testerina.core.AssertionDiffEvaluator"
} external;

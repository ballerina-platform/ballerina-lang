import ballerina/java;

public function testOverloadedConstructorsWithOneParam() returns [handle, handle] {
    handle bufferStrValue = java:fromString("string buffer value");
    handle stringBuffer = newStringBuffer(bufferStrValue);

    handle builderStrValue = java:fromString("string builder value");
    handle stringBuilder = newStringBuilder(builderStrValue);

    handle stringCreatedWithBuffer = newStringWithStringBuffer(stringBuffer);
    handle stringCreatedWithBuilder = newStringWithStringBuilder(stringBuilder);
    return [stringCreatedWithBuffer, stringCreatedWithBuilder];
}

public function newStringBuffer(handle strValue) returns handle = @java:Constructor {
    class:"java.lang.StringBuffer",
    paramTypes:["java.lang.String"]
} external;

public function newStringBuilder(handle strValue) returns handle = @java:Constructor {
    class:"java.lang.StringBuilder",
    paramTypes:["java.lang.String"]
} external;

public function newStringWithStringBuffer(handle buffer) returns handle = @java:Constructor {
    class:"java.lang.String",
    paramTypes:["java.lang.StringBuffer"]
} external;

public function newStringWithStringBuilder(handle builder) returns handle = @java:Constructor {
    class:"java.lang.String",
    paramTypes:["java.lang.StringBuilder"]
} external;

public function testOverloadedMethodsWithByteArrayParams(string strValue) returns string? {
    handle str = java:fromString(strValue);
    handle bytes = getBytes(str);
    sortByteArray(bytes);
    handle sortedStr = newString(bytes);
    return java:toString(sortedStr);
}

const STRING_RESULT = "BALLERINA";
const OBJECT_RESULT = "5";

public function testOverloadedMethodsWithDifferentParameters(handle strValue, any anyValue) {
    string strResult = getString(strValue).toString();
    string objResult = getObjectString(anyValue).toString();
    assertEquality(STRING_RESULT, strResult);
    assertEquality(OBJECT_RESULT, objResult);
}

function getBytes(handle receiver) returns handle = @java:Method {
    class: "java.lang.String"
} external;

function sortByteArray(handle src) = @java:Method {
    name: "sort",
    class: "java.util.Arrays",
    paramTypes: [{class: "byte", dimensions:1}]
} external;

function newString(handle bytes) returns handle = @java:Constructor {
    class: "java.lang.String",
    paramTypes: [{class: "byte", dimensions:1}]
} external;

function getString(handle str) returns handle = @java:Method {
    name: "foo",
    class: "org.ballerinalang.test.javainterop.overloading.pkg.Vehicle",
    paramTypes: ["java.lang.String"]
} external;

function getObjectString(any val) returns handle = @java:Method {
    name: "foo",
    class: "org.ballerinalang.test.javainterop.overloading.pkg.Vehicle",
    paramTypes: ["java.lang.Object"]
} external;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

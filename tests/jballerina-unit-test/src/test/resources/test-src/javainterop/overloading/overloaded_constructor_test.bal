import ballerina/java;

public function testOverloadedConstructorsWithOneParam() returns [handle, handle] {
    handle bufferStrValue = java:toJString("string buffer value");
    handle stringBuffer = newStringBuffer(bufferStrValue);

    handle builderStrValue = java:toJString("string builder value");
    handle stringBuilder = newStringBuilder(builderStrValue);

    handle stringCreatedWithBuffer = newStringWithStringBuffer(stringBuffer);
    handle stringCreatedWithBuilder = newStringWithStringBuilder(stringBuilder);
    return [stringCreatedWithBuffer, stringCreatedWithBuilder];
}

@java:Constructor {class:"java.lang.StringBuffer", paramTypes:["java.lang.String"]}
public function newStringBuffer(handle strValue) returns handle = external;

@java:Constructor {class:"java.lang.StringBuilder", paramTypes:["java.lang.String"]}
public function newStringBuilder(handle strValue) returns handle = external;

@java:Constructor {class:"java.lang.String", paramTypes:["java.lang.StringBuffer"] }
public function newStringWithStringBuffer(handle buffer) returns handle = external;

@java:Constructor {class:"java.lang.String", paramTypes:["java.lang.StringBuilder"]}
public function newStringWithStringBuilder(handle builder) returns handle = external;


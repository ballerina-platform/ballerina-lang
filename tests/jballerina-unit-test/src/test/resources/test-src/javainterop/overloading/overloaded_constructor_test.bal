import ballerinax/java;

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


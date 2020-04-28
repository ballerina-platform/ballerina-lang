import ballerina/java;

function wrapBoolean(boolean b) returns handle = @java:Constructor {
    class: "java.lang.Boolean",
    paramTypes: ["boolean"]
} external;

function wrapByte(byte b) returns handle = @java:Constructor {
    class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

function wrapInt(int i) returns handle = @java:Constructor {
    class: "java.lang.Integer",
    paramTypes: ["int"]
} external;

function wrapFloat(float f) returns handle = @java:Constructor {
    class: "java.lang.Float",
    paramTypes: ["float"]
} external;

function wrapByteToChar(byte b) returns handle = @java:Constructor {
    class: "java.lang.Character",
    paramTypes: ["char"]
} external;

function wrapIntToChar(int b) returns handle = @java:Constructor {
    class: "java.lang.Character",
    paramTypes: ["char"]
} external;

function wrapFloatToChar(float b) returns handle = @java:Constructor {
    class: "java.lang.Character",
    paramTypes: ["char"]
} external;

function wrapFloatToShort(float b) returns handle = @java:Constructor {
    class: "java.lang.Short",
    paramTypes: ["short"]
} external;

function wrapIntToShort(int b) returns handle = @java:Constructor {
    class: "java.lang.Short",
    paramTypes: ["short"]
} external;

function wrapByteToShort(byte b) returns handle = @java:Constructor {
    class: "java.lang.Short",
    paramTypes: ["short"]
} external;

function wrapByteToLong(byte b) returns handle = @java:Constructor {
    class: "java.lang.Long",
    paramTypes: ["long"]
} external;

function wrapIntToLong(int b) returns handle = @java:Constructor {
    class: "java.lang.Long",
    paramTypes: ["long"]
} external;

function wrapFloatToLong(float b) returns handle = @java:Constructor {
    class: "java.lang.Long",
    paramTypes: ["long"]
} external;

function wrapByteToDouble(byte b) returns handle = @java:Constructor {
    class: "java.lang.Double",
    paramTypes: ["double"]
} external;

function wrapFloatToDouble(float b) returns handle = @java:Constructor {
    class: "java.lang.Double",
    paramTypes: ["double"]
} external;

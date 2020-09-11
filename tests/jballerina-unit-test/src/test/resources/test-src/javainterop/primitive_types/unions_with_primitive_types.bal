import ballerina/java;




function readBoolean(handle dataIS) returns boolean | error = @java:Method {
    'class: "java.io.DataInputStream"
} external;

function readByte(handle dataIS) returns byte | error = @java:Method {
    'class: "java.io.DataInputStream"
} external;

function readShort(handle dataIS) returns int | error = @java:Method {
    'class: "java.io.DataInputStream"
} external;

function readChar(handle dataIS) returns int | error = @java:Method {
    'class: "java.io.DataInputStream"
} external;

function readInt(handle dataIS) returns int | error = @java:Method {
    'class: "java.io.DataInputStream"
} external;

function readLong(handle dataIS) returns int | error = @java:Method {
    'class: "java.io.DataInputStream"
} external;

function readFloat(handle dataIS) returns float | error = @java:Method {
    'class: "java.io.DataInputStream"
} external;

function readDouble(handle dataIS) returns float | error = @java:Method {
    'class: "java.io.DataInputStream"
} external;

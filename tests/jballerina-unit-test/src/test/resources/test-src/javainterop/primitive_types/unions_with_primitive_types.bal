import ballerina/java;

function testReadBoolean(handle dataIS) returns boolean | error {
    return readBoolean(dataIS);
}

function testReadByte(handle dataIS) returns byte | error {
    return readByte(dataIS);
}

function testReadShort(handle dataIS) returns int | error {
    return readShort(dataIS);
}

function testReadChar(handle dataIS) returns int | error {
    return readChar(dataIS);
}

function testReadInt(handle dataIS) returns int | error {
    return readInt(dataIS);
}

function testReadLong(handle dataIS) returns int | error {
    return readLong(dataIS);
}

function testReadFloat(handle dataIS) returns float | error {
    return readFloat(dataIS);
}

function testReadDouble(handle dataIS) returns float | error {
    return readDouble(dataIS);
}

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

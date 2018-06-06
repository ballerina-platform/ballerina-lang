import ballerina/io;

function testWriteFixedSignedInt(int value, string path) {
    io:ByteChannel ch = io:openFile(path, io:WRITE);
    io:DataChannel dataChannel = new(ch);
    var result = dataChannel.writeInt64(value);
    var closeResult = ch.close();
}

function testReadFixedSignedInt(string path) returns int|error {
    io:ByteChannel ch = io:openFile(path, io:READ);
    io:DataChannel dataChannel = new(ch);
    int result = check dataChannel.readInt64();
    var closeResult = ch.close();
    return result;
}

function testWriteFixedFloat(float value, string path) {
    io:ByteChannel ch = io:openFile(path, io:WRITE);
    io:DataChannel dataChannel = new(ch);
    var result = dataChannel.writeFloat64(value);
    var closeResult = ch.close();
}

function testReadFixedFloat(string path) returns float|error {
    io:ByteChannel ch = io:openFile(path, io:READ);
    io:DataChannel dataChannel = new(ch);
    float result = check dataChannel.readFloat64();
    var closeResult = ch.close();
    return result;
}

function testWriteBool(boolean value, string path) {
    io:ByteChannel ch = io:openFile(path, io:WRITE);
    io:DataChannel dataChannel = new(ch);
    var result = dataChannel.writeBool(value);
    var closeResult = ch.close();
}

function testReadBool(string path) returns boolean|error {
    io:ByteChannel ch = io:openFile(path, io:READ);
    io:DataChannel dataChannel = new(ch);
    boolean result = check dataChannel.readBool();
    var closeResult = ch.close();
    return result;
}

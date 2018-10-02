import ballerina/io;

function testWriteFixedSignedInt(int value, string path) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch);
    var result = dataChannel.writeInt64(value);
    var closeResult = dataChannel.close();
}

function testReadFixedSignedInt(string path) returns int|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch);
    int result = check dataChannel.readInt64();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteVarInt(int value, string path) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch);
    var result = dataChannel.writeVarInt(value);
    var closeResult = dataChannel.close();
}

function testReadVarInt(string path) returns int|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch);
    int result = check dataChannel.readVarInt();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteFixedFloat(float value, string path) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch);
    var result = dataChannel.writeFloat64(value);
    var closeResult = dataChannel.close();
}

function testReadFixedFloat(string path) returns float|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch);
    float result = check dataChannel.readFloat64();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteBool(boolean value, string path) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch);
    var result = dataChannel.writeBool(value);
    var closeResult = dataChannel.close();
}

function testReadBool(string path) returns boolean|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch);
    boolean result = check dataChannel.readBool();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteString(string path, string content, string encoding) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch);
    var result = check dataChannel.writeString(content, encoding);
    var closeResult = dataChannel.close();
}

function testReadString(string path, int nBytes, string encoding) returns string|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch);
    string result = check dataChannel.readString(nBytes, encoding);
    var closeResult = dataChannel.close();
    return result;
}

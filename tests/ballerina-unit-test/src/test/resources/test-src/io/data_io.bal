import ballerina/io;

function testWriteFixedSignedInt(int value, string path, io:ByteOrder bOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder);
    var result = dataChannel.writeInt64(value);
    var closeResult = dataChannel.close();
}

function testReadFixedSignedInt(string path, io:ByteOrder bOrder) returns int|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder);
    int result = check dataChannel.readInt64();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteVarInt(int value, string path, io:ByteOrder bOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder);
    var result = dataChannel.writeVarInt(value);
    var closeResult = dataChannel.close();
}

function testReadVarInt(string path, io:ByteOrder bOrder) returns int|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder);
    int result = check dataChannel.readVarInt();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteFixedFloat(float value, string path, io:ByteOrder bOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder);
    var result = dataChannel.writeFloat64(value);
    var closeResult = dataChannel.close();
}

function testReadFixedFloat(string path, io:ByteOrder bOrder) returns float|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder);
    float result = check dataChannel.readFloat64();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteBool(boolean value, string path, io:ByteOrder bOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder);
    var result = dataChannel.writeBool(value);
    var closeResult = dataChannel.close();
}

function testReadBool(string path, io:ByteOrder bOrder) returns boolean|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder);
    boolean result = check dataChannel.readBool();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteString(string path, string content, string encoding, io:ByteOrder bOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder);
    var result = check dataChannel.writeString(content, encoding);
    var closeResult = dataChannel.close();
}

function testReadString(string path, int nBytes, string encoding, io:ByteOrder bOrder) returns string|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder);
    string result = check dataChannel.readString(nBytes, encoding);
    var closeResult = dataChannel.close();
    return result;
}

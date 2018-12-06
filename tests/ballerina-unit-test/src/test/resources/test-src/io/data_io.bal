import ballerina/io;

function testWriteFixedSignedInt(int value, string path, io:ByteOrder byteOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    var result = dataChannel.writeInt64(value);
    var closeResult = dataChannel.close();
}

function testReadFixedSignedInt(string path, io:ByteOrder byteOrder) returns int|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    int result = check dataChannel.readInt64();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteVarInt(int value, string path, io:ByteOrder byteOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    var result = dataChannel.writeVarInt(value);
    var closeResult = dataChannel.close();
}

function testReadVarInt(string path, io:ByteOrder byteOrder) returns int|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    int result = check dataChannel.readVarInt();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteFixedFloat(float value, string path, io:ByteOrder byteOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    var result = dataChannel.writeFloat64(value);
    var closeResult = dataChannel.close();
}

function testReadFixedFloat(string path, io:ByteOrder byteOrder) returns float|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    float result = check dataChannel.readFloat64();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteBool(boolean value, string path, io:ByteOrder byteOrder) {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    var result = dataChannel.writeBool(value);
    var closeResult = dataChannel.close();
}

function testReadBool(string path, io:ByteOrder byteOrder) returns boolean|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    boolean result = check dataChannel.readBool();
    var closeResult = dataChannel.close();
    return result;
}

function testWriteString(string path, string content, string encoding, io:ByteOrder byteOrder) returns error? {
    io:WritableByteChannel ch = io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    var result = check dataChannel.writeString(content, encoding);
    var closeResult = dataChannel.close();
    return result;
}

function testReadString(string path, int nBytes, string encoding, io:ByteOrder byteOrder) returns string|error {
    io:ReadableByteChannel ch = io:openReadableFile(path);
    io:ReadableDataChannel dataChannel = new(ch, bOrder = byteOrder);
    string result = check dataChannel.readString(nBytes, encoding);
    var closeResult = dataChannel.close();
    return result;
}

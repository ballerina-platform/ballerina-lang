import ballerina/io;

io:ReadableByteChannel rch = new;
io:WritableByteChannel wch = new;

function initReadableChannel(string filePath) {
    rch = untaint io:openReadableFile(filePath);
}

function initWritableChannel(string filePath) {
    wch = untaint io:openWritableFile(filePath);
}

function readBytes(int numberOfBytes) returns byte[]|error {
    var result = rch.read(numberOfBytes);
    if (result is (byte[], int)) {
        var (bytes, val) = result;
        return bytes;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Unidentified type");
        return e;
    }
}

function writeBytes(byte[] content, int startOffset) returns int|error {
    int empty = -1;
    var result = wch.write(content, startOffset);
    if (result is int) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Unidentified type");
        return e;
    }
}

function closeReadableChannel() {
    var result = rch.close();
}

function closeWritableChannel() {
    var result = wch.close();
}

function testBase64EncodeByteChannel(io:ReadableByteChannel contentToBeEncoded) returns io:ReadableByteChannel|error {
    return contentToBeEncoded.base64Encode();
}

function testBase64DecodeByteChannel(io:ReadableByteChannel contentToBeDecoded) returns io:ReadableByteChannel|error {
    return contentToBeDecoded.base64Decode();
}

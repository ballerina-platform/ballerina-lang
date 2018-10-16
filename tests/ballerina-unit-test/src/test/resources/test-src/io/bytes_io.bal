import ballerina/io;

io:ReadableByteChannel rch;
io:WritableByteChannel wch;

function initReadableChannel(string filePath) {
    rch = untaint io:openReadableFile(filePath);
}

function initWritableChannel(string filePath) {
    wch = untaint io:openWritableFile(filePath);
}

function readBytes(int numberOfBytes) returns byte[]|error {
    byte[] empty;
    var result = rch.read(numberOfBytes);
    match result {
        (byte[], int) content => {
            var (bytes, _) = content;
            return bytes;
        }
        error err => {
            return err;
        }
    }
}

function writeBytes(byte[] content, int startOffset) returns int|error {
    int empty = -1;
    var result = wch.write(content, startOffset);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        error err => {
            return err;
        }
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

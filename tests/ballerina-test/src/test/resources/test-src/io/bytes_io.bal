import ballerina/io;

io:ByteChannel channel;

function initFileChannel (string filePath, io:Mode permission) {
    channel = io:openFile(filePath, permission);
}

function readBytes (int numberOfBytes) returns (blob|error) {
    blob empty;
    var result = channel.read(numberOfBytes);
    match result {
        (blob,int) content =>{
            var (bytes, _) = content;
            return bytes;
        }
        error err =>{
            return err;
        }
    }
}

function writeBytes (blob content, int startOffset) returns (int|error) {
    int empty = -1;
    var result = channel.write(content, startOffset);
    match result {
        int numberOfBytesWritten =>{
            return numberOfBytesWritten;
        }
        error err =>{
            return err;
        }
    }
}

function close () {
    var result = channel.close();
}

function testBase64EncodeByteChannel(io:ByteChannel contentToBeEncoded) returns (io:ByteChannel|error) {
    return contentToBeEncoded.base64Encode();
}

function testBase64DecodeByteChannel(io:ByteChannel contentToBeDecoded) returns (io:ByteChannel|error) {
    return contentToBeDecoded.base64Decode();
}

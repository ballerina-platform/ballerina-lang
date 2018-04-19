import ballerina/io;

io:ByteChannel channel;

function initFileChannel (string filePath, string permission) {
    channel = io:openFile(filePath, permission);
}

function readBytes (int numberOfBytes) returns (blob|io:IOError) {
    blob empty;
    var result = channel.read(numberOfBytes);
    match result {
        (blob,int) content =>{
            var (bytes, _) = content;
            return bytes;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function writeBytes (blob content, int startOffset) returns (int|io:IOError) {
    int empty = -1;
    var result = channel.write(content, startOffset);
    match result {
        int numberOfBytesWritten =>{
            return numberOfBytesWritten;
        }
        io:IOError err =>{
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

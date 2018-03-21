import ballerina/io;

io:ByteChannel channel;

function initFileChannel (string filePath, string permission) {
    channel = io:openFile(filePath, permission);
}

function readBytes (int numberOfBytes) returns (blob|io:IOError) {
    var result = channel.read(numberOfBytes);
    match result {
        (blob, int) content => {
            var (bytes, numberOfBytes) = content;
            return bytes;
        }
        io:IOError err => {
            return err;
        }
    }
}

function writeBytes (blob content, int startOffset) returns (int|io:IOError) {
    var result = channel.write(content, startOffset);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        io:IOError err => {
            return err;
        }
    }
}

function close () {
    io:IOError err = channel.close();
}

import ballerina.io;

io:ByteChannel channel;

function initFileChannel (string filePath, string permission) {
    channel = io:openFile(filePath, permission);
}

function readBytes (int numberOfBytes) returns (blob) {
    var result = channel.read(numberOfBytes);
    match result{
       (blob , int)  content => {
               var (bytes, numberOfBytes) = data;
               return bytes;
       }
       io:IOError error => {
         throw error;
       }
    }
}

function writeBytes (blob content, int startOffset) returns (int) {
    int numberOfBytesWritten;
    var (numberOfBytesWritten, _) = channel.write(content, startOffset);
    return numberOfBytesWritten;
}

function close () {
    channel.close();
}

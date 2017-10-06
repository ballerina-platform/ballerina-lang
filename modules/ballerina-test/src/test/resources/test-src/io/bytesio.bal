import ballerina.lang.files;
import ballerina.io;

io:ByteChannel channel;

function initFileChannel(string filePath, string permission){
    files:File src = {path:filePath};
    files:openAsync(src, permission);
    channel = io:toByteChannel(src);
}

function readBytes (int numberOfBytes) (blob) {
    blob bytes;
    int numberOfBytesRead;
    bytes,numberOfBytesRead = io:readBytes(channel, numberOfBytes);
    return bytes;
}

function writeBytes (blob content, int startOffset) (int) {
    int numberOfBytesWritten = io:writeBytes(channel, content, startOffset);
    return numberOfBytesWritten;
}

function close(){
    io:close(channel);
}


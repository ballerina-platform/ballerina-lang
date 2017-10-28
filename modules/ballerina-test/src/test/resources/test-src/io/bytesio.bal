import ballerina.file;
import ballerina.io;

io:ByteChannel channel;

function initFileChannel(string filePath, string permission){
    file:File src = {path:filePath};
    channel = src.openChannel(permission);
}

function readBytes (int numberOfBytes) (blob) {
    blob bytes;
    int numberOfBytesRead;
    bytes,numberOfBytesRead = channel.readBytes(numberOfBytes);
    return bytes;
}

function writeBytes (blob content, int startOffset) (int) {
    int numberOfBytesWritten = channel.writeBytes(content, startOffset);
    return numberOfBytesWritten;
}

function close(){
    channel.close();
}

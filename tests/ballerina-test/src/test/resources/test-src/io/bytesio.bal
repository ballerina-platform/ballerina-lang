import ballerina.file;
import ballerina.io;

io:ByteChannel channel;

function initFileReadChannel(string filePath){
    file:File src = {path:filePath};
    channel = src.openChannel(file:AccessMode.R);
}

function initFileWriteChannel(string filePath){
    file:File src = {path:filePath};
    channel = src.openChannel(file:AccessMode.W);
}

function readAll() (blob) {
    blob bytes;
    int numberOfBytesRead;
    bytes,numberOfBytesRead = channel.readAllBytes();
    return bytes;
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

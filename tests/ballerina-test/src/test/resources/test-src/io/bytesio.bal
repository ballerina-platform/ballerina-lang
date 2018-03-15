import ballerina.io;

io:ByteChannel channel;

function initFileChannel (string filePath, string permission) {
    channel = io:openFile(filePath, permission);
}

function readBytes (int numberOfBytes) (byte[]) {
    byte[] content = [];
    int offset = 0;
    int numberOfBytesRead = 0;
    boolean hasRemaining = true;
    while(hasRemaining){
      numberOfBytesRead, _ = channel.read(content, numberOfBytes, offset);
      offset = offset + numberOfBytesRead;
      if(numberOfBytesRead == 0 || offset == numberOfBytes) {
        hasRemaining = false;
      }
    }
    return content;
}

function writeBytes (byte[] content,int size, int startOffset) (int) {
    int numberOfBytesWritten;
    numberOfBytesWritten, _ = channel.write(content, size, startOffset);
    return numberOfBytesWritten;
}

function close () {
    _ = channel.close();
}

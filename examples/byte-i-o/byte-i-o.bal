import ballerina.io;

@Description {value:"This function returns a ByteChannel from a given file location according to the specified file permission (whether the file should be opened for reading/writing)."}
function getFileChannel (string filePath, string permission) (io:ByteChannel) {
    // Here is how the ByteChannel is retrieved from the file.
    io:ByteChannel channel = io:openFile(filePath, permission);
    return channel;
}

@Description {value:"This function reads the specified number of bytes from the given channel."}
function readBytes (io:ByteChannel channel, int numberOfBytes, int offset) (blob, int, io:IOError) {
    blob bytes;
    int numberOfBytesRead;
    io:IOError readError;
    // Here is how the bytes are read from the channel.
    bytes, numberOfBytesRead, readError = channel.read(numberOfBytes, offset);
    return bytes, numberOfBytesRead, readError;
}

@Description {value:"This function writes a byte content with the given offset to a channel."}
function writeBytes (io:ByteChannel channel, blob content, int startOffset, int size) (int, io:IOError) {
    int numberOfBytesWritten;
    io:IOError writeError;
    // Here is how the bytes are written to the channel.
    numberOfBytesWritten, writeError = channel.write(content, startOffset, size);
    return numberOfBytesWritten, writeError;
}

@Description {value:"This function copies content from the source channel to a destination channel."}
function copy (io:ByteChannel src, io:ByteChannel dst) {
    // Specifies the number of bytes that should be read from a single read operation.
    int bytesChunk = 10000;
    int numberOfBytesWritten = 0;
    int readCount = 0;
    int offset = 0;
    blob readContent;
    boolean done = false;
    io:IOError readError;
    io:IOError writeError;
    // Here is how to specify to read all the content from
    // the source and copy it to the destination.
    while (!done) {
        readContent, readCount, readError = readBytes(src, bytesChunk, offset);
        io:println(readCount);
        if (readCount <= 0) {
            readCount = 0;
            done = true;
        }
        if (readError != null) {
            io:println(readError);
            break;
        }
        numberOfBytesWritten, writeError = writeBytes(dst, readContent, offset, readCount);
        if (writeError != null) {
            io:println(writeError);
            break;
        }
    }
}

function main (string[] args) {
    // Read specified number of bytes from the given channel and write.
    string srcFilePath = "./files/ballerina.jpg";
    string dstFilePath = "./files/ballerinaCopy.jpg";
    io:ByteChannel sourceChannel = getFileChannel(srcFilePath, "r");
    io:ByteChannel destinationChannel = getFileChannel(dstFilePath, "w");
    io:IOError srcCloseError;
    io:IOError dstCloseError;
    io:println("Start to copy files from " + srcFilePath + " to " + dstFilePath);
    copy(sourceChannel, destinationChannel);
    io:println("File copy completed. The copied file could be located in " + dstFilePath);
    // Close the created connections.
    srcCloseError = sourceChannel.close();
    dstCloseError = destinationChannel.close();
}

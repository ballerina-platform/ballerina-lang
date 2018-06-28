import ballerina/io;

// This function returns a ByteChannel from a given file location
// according to the specified file permission (i.e., whether the file
// should be opened for read or write).
function getFileChannel(string filePath,
                        io:Mode permission) returns io:ByteChannel {
    // Here is how the ByteChannel is retrieved from the file.
    io:ByteChannel channel = io:openFile(filePath, permission);
    return channel;
}

// Reads a specified number of bytes from the given channel.
function readBytes(io:ByteChannel channel,
                   int numberOfBytes) returns (blob, int) {
    // Here is how the bytes are read from the channel.
    var result = channel.read(numberOfBytes);
    match result {
        (blob, int) content => {
            return content;
        }
        error readError => {
            throw readError;
        }
    }
}

// Writes byte content with the given offset to a channel.
function writeBytes(io:ByteChannel channel,
                    blob content,
                    int startOffset = 0) returns int {
    // Here is how the bytes are written to the channel.
    var result = channel.write(content, startOffset);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        error err => {
            throw err;
        }
    }
}

// Copies content from the source channel to a destination channel.
function copy(io:ByteChannel src, io:ByteChannel dst) {
    // Specifies the number of bytes that should be read from a single
    // read operation.
    int bytesChunk = 10000;
    int numberOfBytesWritten = 0;
    int readCount = 0;
    int offset = 0;
    blob readContent;
    boolean doneCopying = false;
    try {
        // Here is how to read all the content from
        // the source and copy it to the destination.
        while (!doneCopying) {
            (readContent, readCount) = readBytes(src, 1000);
            if (readCount <= 0) {
                //If no content is read, the loop is ended.
                doneCopying = true;
            }
            numberOfBytesWritten = writeBytes(dst, readContent);
        }
    } catch (error err) {
        throw err;
    }
}

function main(string... args) {
    string srcFilePath = "./files/ballerina.jpg";
    string dstFilePath = "./files/ballerinaCopy.jpg";
    io:ByteChannel sourceChannel = getFileChannel(srcFilePath, io:READ);
    io:ByteChannel destinationChannel = getFileChannel(dstFilePath, io:WRITE);
    try {
        io:println("Start to copy files from " + srcFilePath + " to " +
                dstFilePath);
        copy(sourceChannel, destinationChannel);
        io:println("File copy completed. The copied file could be located in " +
                dstFilePath);
    } catch (error err) {
        io:println("error occurred while performing copy " + err.message);
    } finally {
        // Close the created connections.
        match sourceChannel.close() {
            error sourceCloseError => {
                io:println("Error occured while closing the channel: " +
                        sourceCloseError.message);
            }
            () => {
                io:println("Source channel closed successfully.");
            }
        }
        match destinationChannel.close() {
            error destinationCloseError => {
                io:println("Error occured while closing the channel: " +
                        destinationCloseError.message);
            }
            () => {
                io:println("Destination channel closed successfully.");
            }
        }
    }
}

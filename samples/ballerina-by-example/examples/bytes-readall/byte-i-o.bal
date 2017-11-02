import ballerina.file;
import ballerina.io;

@Description{value:"This function will return a ByteChannel from a given file location according to the specified file permission whether the file should be opened for reading/writing."}
function getFileChannel (string filePath, string permission) (io:ByteChannel) {
    //Here's how the path of the file will be specified
    file:File src = {path:filePath};
    //Here's how the ByteChannel is retrieved from the file
    io:ByteChannel channel = src.openChannel(permission);
    return channel;
}

@Description{value:"This function will copy all content from source channel to a destination channel."}
function copy (io:ByteChannel src, io:ByteChannel dst) {
    blob readContent;
    int numberOfBytesRead;
    //Here's how we read all content from the source
    readContent,numberOfBytesRead = src.readAllBytes();
    int numberOfBytesWritten = dst.writeBytes(readContent, 0);
}

function main (string[] args) {
    string srcFilePath = "./files/ballerina.jpg";
    string dstFilePath = "./files/ballerinaCopy.jpg";
    io:ByteChannel sourceChannel = getFileChannel(srcFilePath, "r");
    io:ByteChannel destinationChannel = getFileChannel(dstFilePath, "w");
    println("Start to copy files from " + srcFilePath + " to " + dstFilePath);
    copy(sourceChannel, destinationChannel);
    println("File copy completed. The copied file could be located in " + dstFilePath);
    //Close the created connections
    sourceChannel.close();
    destinationChannel.close();
}

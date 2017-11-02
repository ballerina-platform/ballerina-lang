import ballerina.file;
import ballerina.io;

@Description{value:"This function will return a CharacterChannel from a given file location accoring to the specified permissions and encoding."}
function getFileCharacterChannel (string filePath, string permission, string encoding)
                                 (io:CharacterChannel) {
    file:File src = {path:filePath};
    //First we get the ByteChannel representation of the file
    io:ByteChannel channel = src.openChannel(permission);
    //Then we convert the byte channel to character channel to read content as text
    io:CharacterChannel characterChannel = channel.toCharacterChannel(encoding);
    return characterChannel;
}

function main (string[] args) {
    io:CharacterChannel sourceChannel =
    getFileCharacterChannel("./files/sample.txt", "r", "UTF-8");
    io:CharacterChannel destinationChannel =
    getFileCharacterChannel("./files/sampleResponse.txt", "w", "UTF-8");
    println("Started to read all characters in file");
    string sourceContent = sourceChannel.readAllCharacters();
    _ = destinationChannel.writeCharacters(sourceContent,0);
    println("All characters are read and copied");
    //Close the created connections
    sourceChannel.closeCharacterChannel();
    destinationChannel.closeCharacterChannel();
}

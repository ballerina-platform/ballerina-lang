import ballerina.io;

@Description{value:"This function will return a CharacterChannel from a given file location according to the specified permissions and encoding."}
function getFileCharacterChannel (string filePath, string permission, string encoding)
(io:CharacterChannel) {
    //First we get the ByteChannel representation of the file.
    io:ByteChannel channel = io:openFile(filePath, permission);
    //Then we create a character channel from the byte channel to read content as text.
    io:CharacterChannel characterChannel = io:createCharacterChannel(channel, encoding);
    return characterChannel;
}

@Description{value:"This function will read content from a file, append the additional string and write content."}
function process (io:CharacterChannel sourceChannel,
                  io:CharacterChannel destinationChannel) {
    //Here's the string which will be appended in-between.
    string intermediateCharacterString = " my name is ";
    //First 5 characters in the source file contains the greeting.
    string greetingText = sourceChannel.readCharacters(5);
    //We request for the next set of characters in the file.
    string name = sourceChannel.readCharacters(12);
    //Here's how the greeting is written to the destination file.
    _ = destinationChannel.writeCharacters(greetingText, 0);
    //Here's how the intermediate string is appended to the file.
    _ = destinationChannel.writeCharacters(intermediateCharacterString, 0);
    //Here's how the remaining characters are written to the file, leaving an offset.
    _ = destinationChannel.writeCharacters(name, 1);
}

function main (string[] args) {
    io:CharacterChannel sourceChannel =
    getFileCharacterChannel("./files/sample.txt", "r", "UTF-8");
    io:CharacterChannel destinationChannel =
    getFileCharacterChannel("./files/sampleResponse1.txt", "w", "UTF-8");
    io:println("Started to process the file.");
    process(sourceChannel, destinationChannel);
    io:println("File processing complete.");
    //Close the created connections.
    sourceChannel.closeCharacterChannel();
    destinationChannel.closeCharacterChannel();

    //Read all characters in a file and write to the source file.
    sourceChannel =
    getFileCharacterChannel("./files/sample.txt", "r", "UTF-8");
    destinationChannel =
    getFileCharacterChannel("./files/sampleResponse2.txt", "w", "UTF-8");
    io:println("Started to read all characters in file.");
    string sourceContent = sourceChannel.readAllCharacters();
    _ = destinationChannel.writeCharacters(sourceContent,0);
    io:println("All characters are read and copied.");
    //Close the created connections.
    sourceChannel.closeCharacterChannel();
    destinationChannel.closeCharacterChannel();
}
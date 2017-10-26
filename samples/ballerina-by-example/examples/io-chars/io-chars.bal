import ballerina.lang.files;
import ballerina.io;

function getFileCharacterChannel(string filePath,string permission,string encoding)(io:CharacterChannel){
    files:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
    io:CharacterChannel characterChannel = channel.toCharacterChannel(encoding);
    return characterChannel;
}

function process(io:CharacterChannel sourceChannel,io:CharacterChannel destinationChannel){
    string intermediateCharacterString = " my name is ";
    string greetingText = sourceChannel.readCharacters(5);
    string name = sourceChannel.readCharacters(12);
    _=destinationChannel.writeCharacters(greetingText, 0);
    _=destinationChannel.writeCharacters(intermediateCharacterString, 0);
    _=destinationChannel.writeCharacters(name, 1);
}

function main (string[] args) {
    io:CharacterChannel sourceChannel = getFileCharacterChannel("./files/sample.txt", "r","UTF-8");
    io:CharacterChannel destinationChannel = getFileCharacterChannel("./files/sampleResponse.txt", "w","UTF-8");
    process(sourceChannel, destinationChannel);
}

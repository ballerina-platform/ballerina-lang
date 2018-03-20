import ballerina.io;

io:CharacterChannel characterChannel;

function initFileChannel (string filePath, string permission, string encoding) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    characterChannel, _ = io:createCharacterChannel(channel, encoding);
}

function readCharacters (int numberOfCharacters) (string) {
    string characters;
    characters, _ = characterChannel.readCharacters(numberOfCharacters);
    return characters;
}

function writeCharacters (string content, int startOffset) (int) {
    int numberOfCharactersWritten;
    numberOfCharactersWritten, _ = characterChannel.writeCharacters(content, startOffset);
    return numberOfCharactersWritten;
}

function close(){
    _ = characterChannel.closeCharacterChannel();
}

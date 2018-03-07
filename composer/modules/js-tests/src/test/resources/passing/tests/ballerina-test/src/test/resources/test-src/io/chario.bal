import ballerina.io;

io:CharacterChannel characterChannel;

function initFileChannel(string filePath,string permission,string encoding){
    io:ByteChannel channel = io:openFile(filePath, permission);
    characterChannel = io:createCharacterChannel(channel, encoding);
}

function readAll()(string){
    string characters = characterChannel.readAllCharacters();
    return characters;
}

function readCharacters (int numberOfCharacters) (string) {
    string characters = characterChannel.readCharacters(numberOfCharacters);
    return characters;
}

function writeCharacters (string content, int startOffset) (int) {
    int numberOfCharactersWritten = characterChannel.writeCharacters(content, startOffset);
    return numberOfCharactersWritten;
}

function close(){
    characterChannel.closeCharacterChannel();
}

import ballerina.file;
import ballerina.io;

io:CharacterChannel characterChannel;

function initFileReadChannel(string filePath, string encoding){
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(file:AccessMode.R);
    characterChannel = channel.toCharacterChannel(encoding);
}

function initFileWriteChannel(string filePath, string encoding){
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(file:AccessMode.W);
    characterChannel = channel.toCharacterChannel(encoding);
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

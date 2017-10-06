import ballerina.lang.files;
import ballerina.io;

io:CharacterChannel characterChannel;

function initFileChannel(string filePath,string permission,string encoding){
    files:File src = {path:filePath};
    files:openAsync(src, permission);
    io:ByteChannel channel = io:toByteChannel(src);
    characterChannel = io:toCharacterChannel(channel,encoding);
}

function readCharacters (int numberOfCharacters) (string) {
    string characters = io:readCharacters(characterChannel, numberOfCharacters);
    return characters;
}

function writeCharacters (string content, int startOffset) (int) {
    int numberOfCharactersWritten = io:writeCharacters(characterChannel, content, startOffset);
    return numberOfCharactersWritten;
}




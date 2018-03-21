import ballerina/io;

io:CharacterChannel characterChannel;

function initFileChannel (string filePath, string permission, string encoding) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    var result = io:createCharacterChannel(channel, encoding);
    match result{
         io:CharacterChannel charChannel => {
             characterChannel = charChannel;
         }
         io:IOError err => {
             throw err;
         }
    }
}

function readCharacters (int numberOfCharacters) returns (string) {
    var result = characterChannel.readCharacters(numberOfCharacters);
    match result{
       string characters => {
          return characters;
       }
       io:IOError err => {
          throw err;
       }
    }
    return "";
}

function writeCharacters (string content, int startOffset) returns (int) {
    var result = characterChannel.writeCharacters(content, startOffset);
    match result {
        int numberOfCharsWritten =>{
           return numberOfCharsWritten;
        }
        io:IOError err => {
           throw err;
        }
    }
    return -1;
}

function close(){
    io:IOError err = characterChannel.closeCharacterChannel();
}

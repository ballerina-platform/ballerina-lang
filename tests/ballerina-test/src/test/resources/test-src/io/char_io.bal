import ballerina/io;

io:CharacterChannel characterChannel;

function initCharacterChannel (string filePath, string permission, string encoding) returns (boolean|io:IOError) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    var result = io:createCharacterChannel(channel, encoding);
    match result {
        io:CharacterChannel charChannel =>{
            characterChannel = charChannel;
            return true;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function readCharacters (int numberOfCharacters) returns (string|io:IOError) {
    var result = characterChannel.readCharacters(numberOfCharacters);
    match result {
        string characters =>{
            return characters;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function writeCharacters (string content, int startOffset) returns (int|io:IOError) {
    var result = characterChannel.writeCharacters(content, startOffset);
    match result {
        int numberOfCharsWritten =>{
            return numberOfCharsWritten;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function readJson () returns (json|io:IOError) {
    var result = characterChannel.readJson();
    match result {
        json characters =>{
            return characters;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function readXml () returns (xml|io:IOError) {
    var result = characterChannel.readXml();
    match result {
        xml characters =>{
            return characters;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function close () {
    var err = characterChannel.closeCharacterChannel();
}

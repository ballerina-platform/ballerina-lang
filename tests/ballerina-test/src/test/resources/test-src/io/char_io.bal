import ballerina/io;

io:CharacterChannel|null characterChannel;

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
    string empty = "";
    match characterChannel {
        io:CharacterChannel ch =>{
            var result = ch.readCharacters(numberOfCharacters);
            match result {
                string characters =>{
                    return characters;
                }
                io:IOError err =>{
                    return err;
                }
            }
        }
        (any|null) =>{
            return empty;
        }
    }
}

function writeCharacters (string content, int startOffset) returns (int|io:IOError) {
    int blank = -1;
    match characterChannel {
        io:CharacterChannel ch =>{
            var result = ch.writeCharacters(content, startOffset);
            io:println(result);
            match result {
                int numberOfCharsWritten =>{
                    return numberOfCharsWritten;
                }
                io:IOError err =>{
                    return err;
                }
            }
        }
        (any|null) =>{
            return blank;
        }
    }
}

function readJson () returns (json|io:IOError) {
    json empty = "";
    match characterChannel {
        io:CharacterChannel ch =>{
            var result = ch.readJson();
            match result {
                json characters =>{
                    return characters;
                }
                io:IOError err =>{
                    return err;
                }
            }
        }
        (any|null) =>{
            return empty;
        }
    }
}

function readXml () returns (xml|io:IOError) {
    xml empty = xml `<name/>`;
    match characterChannel {
        io:CharacterChannel ch =>{
            var result = ch.readXml();
            match result {
                xml characters =>{
                    return characters;
                }
                io:IOError err =>{
                    return err;
                }
            }
        }
        (any|null) =>{
            return empty;
        }
    }
}

function close () {
    match characterChannel {
        io:CharacterChannel ch =>{
            io:IOError err = ch.closeCharacterChannel();
        }
        (any|null) =>{
            io:println("Channel cannot be closed");
        }
    }
}

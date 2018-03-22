import ballerina/io;

io:CharacterChannel|null chChannel;

function initCharacterChannel (string filePath, string permission, string encoding) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    var result = io:createCharacterChannel(channel, encoding);
    match result {
        io:CharacterChannel charChannel => {
            chChannel = charChannel;
        }
        io:IOError err => {
            throw err;
        }
    }
}

function readCharacters (int numberOfCharacters) returns (string) {
    string empty = "";
    match chChannel {
        io:CharacterChannel ch => {
            var result = ch.readCharacters(numberOfCharacters);
            match result {
                string characters => {
                    return characters;
                }
                io:IOError err => {
                    throw err;
                }
            }
            return empty;
        }
        (any|null) => {
            return empty;
        }
    }
}

function writeCharacters (string content, int startOffset) returns (int) {
    int blank = -1;
    match chChannel {
        io:CharacterChannel ch => {
            var result = ch.writeCharacters(content, startOffset);
            io:println(result);
            match result {
                int numberOfCharsWritten => {
                    return numberOfCharsWritten;
                }
                io:IOError err => {
                    throw err;
                }
            }
            return blank;
        }
        (any|null) => {
            return blank;
        }
    }
}

function close () {
    match chChannel {
        io:CharacterChannel ch => {
            io:IOError err = ch.closeCharacterChannel();
        }
        (any|null) => {
            io:println("Channel cannot be closed");
        }
    }
}

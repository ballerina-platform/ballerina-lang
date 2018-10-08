import ballerina/io;

io:CharacterChannel? characterChannel;

function initCharacterChannel(string filePath, io:Mode permission, string encoding) {
    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    characterChannel = untaint new io:CharacterChannel(byteChannel, encoding);
}

function readCharacters(int numberOfCharacters) returns string|error {
    var result = characterChannel.read(numberOfCharacters);
    match result {
        string characters => {
            return characters;
        }
        error err => {
            return err;
        }
        () => {
            error e = {message:"Character channel not initialized properly"};
            return e;
        }
    }
}

function readAllCharacters() returns string|error? {
    int fixedSize = 500;
    boolean isDone = false;
    string result;
    while (!isDone){
        match readCharacters(fixedSize) {
            string value => {
                result = result + value;
            }
            error err => {
                if (err.message == "io.EOF"){
                    isDone = true;
                } else {
                    return err;
                }
            }
        }
    }
    return result;
}

function writeCharacters(string content, int startOffset) returns int|error? {
    var result = characterChannel.write(content, startOffset);
    match result {
        int numberOfCharsWritten => {
            return numberOfCharsWritten;
        }
        error err => {
            return err;
        }
        () => {
            error e = {message:"Character channel not initialized properly"};
            return e;
        }

    }
}

function readJson() returns json|error {
    var result = characterChannel.readJson();
    match result {
        json characters => {
            return characters;
        }
        error err => {
            return err;
        }
    }
}

function readXml() returns xml|error {
    var result = characterChannel.readXml();
    match result {
        xml characters => {
            return characters;
        }
        error err => {
            return err;
        }
        () => {
            error e = {message:"Character channel not initialized properly"};
            return e;
        }
    }
}

function writeJson(json content) {
    var result = characterChannel.writeJson(content);
}

function writeXml(xml content) {
    var result = characterChannel.writeXml(content);
}

function close() {
    var err = characterChannel.close();
}

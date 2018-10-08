import ballerina/io;

io:ReadableCharacterChannel? rch;
io:WritableCharacterChannel? wch;

function initReadableChannel(string filePath, string encoding) {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    rch = untaint new io:ReadableCharacterChannel(byteChannel, encoding);
}

function initWritableChannel(string filePath, string encoding) {
    io:WritableByteChannel byteChannel = io:openWritableFile(filePath);
    wch = untaint new io:WritableCharacterChannel(byteChannel, encoding);
}

function readCharacters(int numberOfCharacters) returns string|error {
    var result = rch.read(numberOfCharacters);
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
    var result = wch.write(content, startOffset);
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
    var result = rch.readJson();
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
    var result = rch.readXml();
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
    var result = wch.writeJson(content);
}

function writeXml(xml content) {
    var result = wch.writeXml(content);
}

function closeReadableChannel() {
    var err = rch.close();
}

function closeWritableChannel() {
    var err = wch.close();
}

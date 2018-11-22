import ballerina/io;

io:ReadableCharacterChannel? rch = ();
io:WritableCharacterChannel? wch = ();

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
    if (result is string) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Character channel not initialized properly");
        return e;
    }
}

function readAllCharacters() returns string|error? {
    int fixedSize = 500;
    boolean isDone = false;
    string result = "";
    while (!isDone) {
        var readResult = readCharacters(fixedSize);
        if (readResult is string) {
            result = result + readResult;
        } else if (readResult is error) {
            if (<string>readResult.detail()["message"] == "io.EOF") {
                isDone = true;
            } else {
                return readResult;
            }
        }
    }
    return result;
}

function writeCharacters(string content, int startOffset) returns int|error? {
    var result = wch.write(content, startOffset);
    if (result is int) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Character channel not initialized properly");
        return e;
    }
}

function readJson() returns json|error {
    var result = rch.readJson();
    if (result is json) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Unidentified type");
        return e;
    }
}

function readXml() returns xml|error {
    var result = rch.readXml();
    if (result is xml) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Character channel not initialized properly");
        return e;
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

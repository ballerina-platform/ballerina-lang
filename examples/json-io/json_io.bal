import ballerina/io;
import ballerina/log;

// Closes a readable channel.
function closeRc(io:ReadableCharacterChannel rc) {
    var result = rc.close();
    if (result is error) {
        log:printError("Error occurred while closing character stream",
                        err = result);
    }
}

// Closes a writable channel.
function closeWc(io:WritableCharacterChannel wc) {
    var result = wc.close();
    if (result is error) {
        log:printError("Error occurred while closing character stream",
                        err = result);
    }
}

// Writes the provided `json` to the specified path.
function write(json content, string path) returns error? {
    // Creates a writable byte channel from the given path.
    io:WritableByteChannel wbc = io:openWritableFile(path);
    // Derives the character channel from the byte channel.
    io:WritableCharacterChannel wch = new(wbc, "UTF8");
    var result = wch.writeJson(content);
    if (result is error) {
        closeWc(wch);
        return result;
    } else {
        closeWc(wch);
        return result;
    }
}

// Reads a `json` value from the specified path.
function read(string path) returns json|error {
    // Creates a readable byte channel from the given path.
    io:ReadableByteChannel rbc = io:openReadableFile(path);
    // Derives the character channel from the byte channel.
    io:ReadableCharacterChannel rch = new(rbc, "UTF8");
    var result = rch.readJson();
    if (result is error) {
        closeRc(rch);
        return result;
    } else {
        closeRc(rch);
        return result;
    }
}

public function main() {
    string filePath = "./files/sample.json";
    // Creates `json` content from `string`.
    json j1 = { "Store": {
        "@id": "AST",
        "name": "Anne",
        "address": {
            "street": "Main",
            "city": "94"
        },
        "codes": ["4", "8"]
    }
    };
    io:println("Preparing to write json file");
    // Writes the content.
    var wResult = write(j1, filePath);
    if (wResult is error) {
        log:printError("Error occurred while writing json: ", err = wResult);
    } else {
        io:println("Preparing to read the content written");
        // Reads the content.
        var rResult = read(filePath);
        if (rResult is error) {
            log:printError("Error occurred while reading json: ",
                            err = rResult);
        } else {
            io:println(rResult);
        }
    }
}

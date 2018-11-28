import ballerina/io;
import ballerina/log;

function closeRc(io:ReadableCharacterChannel rc) {
    var result = rc.close();
    if (result is error) {
        log:printError("Error occurred while closing character stream",
            err = result);
    }
}

function closeWc(io:WritableCharacterChannel wc) {
    var result = wc.close();
    if (result is error) {
        log:printError("Error occurred while closing character stream",
            err = result);
    }
}

function write(json content, string path) returns error? {
    // Create a writable byte channel from the given path
    io:WritableByteChannel wbc = io:openWritableFile(path);
    // Derive the character channel from the byte channel
    io:WritableCharacterChannel wch = new(wbc, "UTF8");
    // This is how json content is written via the character channel
    return wch.writeJson(content);
}

function read(string path) returns json|error {
    // Create a readable byte channel from the given path
    io:ReadableByteChannel rbc = io:openReadableFile(path);
    // Derive the character channel from the byte channel
    io:ReadableCharacterChannel rch = new(rbc, "UTF8");
    // This is how json content is read from the character channel
    return rch.readJson();
}

public function main() {
    string filePath = "./files/sample.json";
    //Create json content from string
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
    // Write the content
    var wResult = write(j1, filePath);
    if (wResult is error) {
        log:printError("Error occurred while writing json: ", err = wResult);
    } else {
        io:println("Preparing to read the content written");
        // Read the content
        var rResult = read(filePath);
        if (rResult is error) {
            log:printError("Error occurred while reading json: ",
                err = rResult);
        } else if (rResult is json){
            io:println(rResult);
        }
    }
}

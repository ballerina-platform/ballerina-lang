import ballerina/io;
import ballerina/log;

function close(io:CharacterChannel characterChannel) {
    // Close the character channel when done
    characterChannel.close() but {
        error e => log:printError("Error occurred while closing character stream", err = e)
    };
}

function write(json content, string path) {
    // Create a byte channel from the given path
    io:ByteChannel byteChannel = io:openFile(path, io:WRITE);
    // Derive the character channel from the byte channel
    io:CharacterChannel characterChannel = new io:CharacterChannel(byteChannel, "UTF8");
    // This is how json content is written via the character channel
    match characterChannel.writeJson(content) {
        error err => {
            close(characterChannel);
            throw err;
        }
        () => {
            close(characterChannel);
            io:println("Content written successfully");
        }
    }
}

function read(string path) returns json {
    // Create a byte channel from the given path
    io:ByteChannel byteChannel = io:openFile(path, io:READ);
    // Derive the character channel from the byte channel
    io:CharacterChannel characterChannel = new io:CharacterChannel(byteChannel, "UTF8");
    // This is how json content is read from the character channel
    match characterChannel.readJson() {
        json result => {
            close(characterChannel);
            return result;
        }
        error err => {
            close(characterChannel);
            throw err;
        }
    }
}

function main(string... args) {
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
    write(j1, filePath);
    io:println("Preparing to read the content written");
    // Read the content
    json content = read(filePath);
    io:println(content);
}

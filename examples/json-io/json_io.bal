import ballerina/io;
import ballerina/log;

function close(io:ReadableCharacterChannel|io:WritableCharacterChannel
               characterChannel) {
    // Close the character channel when done
    match characterChannel {
        io:ReadableCharacterChannel readableCharChannel => {
            readableCharChannel.close() but {
                error e =>
                log:printError("Error occurred while closing character stream",
                    err = e)
            };
        }
        io:WritableCharacterChannel writableCharChannel => {
            writableCharChannel.close() but {
                error e =>
                log:printError("Error occurred while closing character stream",
                    err = e)
            };
        }
    }

}

function write(json content, string path) {
    // Create a writable byte channel from the given path
    io:WritableByteChannel byteChannel = io:openWritableFile(path);
    // Derive the character channel from the byte channel
    io:WritableCharacterChannel ch = new io:WritableCharacterChannel(byteChannel, "UTF8");
    // This is how json content is written via the character channel
    match ch.writeJson(content) {
        error err => {
            close(ch);
            throw err;
        }
        () => {
            close(ch);
            io:println("Content written successfully");
        }
    }
}

function read(string path) returns json {
    // Create a readable byte channel from the given path
    io:ReadableByteChannel byteChannel = io:openReadableFile(path);
    // Derive the character channel from the byte channel
    io:ReadableCharacterChannel ch = new io:ReadableCharacterChannel(byteChannel, "UTF8");
    // This is how json content is read from the character channel
    match ch.readJson() {
        json result => {
            close(ch);
            return result;
        }
        error err => {
            close(ch);
            throw err;
        }
    }
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
    write(j1, filePath);
    io:println("Preparing to read the content written");
    // Read the content
    json content = read(filePath);
    io:println(content);
}

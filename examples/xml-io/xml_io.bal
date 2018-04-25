import ballerina/io;
import ballerina/log;

function close(io:CharacterChannel characterChannel) {
    // Close the character channel when done
    characterChannel.close() but {
        error e => log:printError("Error occurred while closing character stream", err = e)
    };
}

function write(xml content, string path) {
    // Create a byte channel from the given path
    io:ByteChannel byteChannel = io:openFile(path, io:WRITE);
    // Derive the character channel from the byte Channel
    io:CharacterChannel characterChannel = new io:CharacterChannel(byteChannel, "UTF8");
    // This is how xml content is written via the character channel
    match characterChannel.writeXml(content) {
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

function read(string path) returns xml {
    // Create a byte channel from the given path
    io:ByteChannel byteChannel = io:openFile(path, io:READ);
    // Derive the character channel from the byte Channel
    io:CharacterChannel characterChannel = new io:CharacterChannel(byteChannel, "UTF8");
    // This is how xml content is read from the character channel
    match characterChannel.readXml() {
        xml result => {
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
    string filePath = "./files/sample.xml";
    // Create an xml from the string
    xml x1 = xml `<book>The Lost World</book>`;
    io:println("Preparing to write xml file");
    // Write the content
    write(x1, filePath);
    io:println("Preparing to read the content written");
    // Read the content
    xml content = read(filePath);
    io:println(content);
}

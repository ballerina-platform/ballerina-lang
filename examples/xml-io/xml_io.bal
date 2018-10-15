import ballerina/io;
import ballerina/log;

function close(io:ReadableCharacterChannel|io:WritableCharacterChannel
               characterChannel) {
    // Close the character channel when done
    match characterChannel {
        io:ReadableCharacterChannel readableChannel => {
            readableChannel.close() but {
                error e =>
                log:printError("Error occurred while closing readable character stream",
                    err = e)
            };
        }
        io:WritableCharacterChannel writableChannel => {
            writableChannel.close() but {
                error e =>
                log:printError("Error occurred while closing writable character stream",
                    err = e)
            };
        }
    }
}

function write(xml content, string path) {
    // Create a byte channel from the given path
    io:WritableByteChannel byteChannel = io:openWritableFile(path);
    // Derive the character channel from the byte Channel
    io:WritableCharacterChannel ch = new io:WritableCharacterChannel(byteChannel, "UTF8");
    // This is how XML content is written via the character channel
    match ch.writeXml(content) {
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

function read(string path) returns xml {
    // Create a byte channel from the given path
    io:ReadableByteChannel byteChannel = io:openReadableFile(path);
    // Derive the character channel from the byte Channel
    io:ReadableCharacterChannel ch = new io:ReadableCharacterChannel(byteChannel, "UTF8");
    // This is how XML content is read from the character channel
    match ch.readXml() {
        xml result => {
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
    string filePath = "./files/sample.xml";
    // Create XML content from the string
    xml x1 = xml `<book>The Lost World</book>`;
    io:println("Preparing to write xml file");
    // Write the content
    write(x1, filePath);
    io:println("Preparing to read the content written");
    // Read the content
    xml content = read(filePath);
    io:println(content);
}

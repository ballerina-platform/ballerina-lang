import ballerina/io;

// This function reads characters from 'charChannel',
//which is an instance of ReadableCharacterChannel.
function readCharacters(io:ReadableCharacterChannel charChannel,
                        int numberOfChars) returns string {
    //This is how the characters are read.
    match charChannel.read(numberOfChars) {
        string characters => {
            return characters;
        }
        error err => {
            throw err;
        }
    }
}

// This function wrties characters to 'charChannel'
function writeCharacters(io:WritableCharacterChannel charChannel, string content,
                         int startOffset) {
    //This is how the characters are written.
    match charChannel.write(content, startOffset) {
        int numberOfCharsWritten => {
            io:println(" No of characters written : " + numberOfCharsWritten);
        }
        error err => {
            throw err;
        }
    }
}

// This function reads content from a file,
// appends the additional string, and writes the content.
function process(io:ReadableCharacterChannel sourceChannel,
                 io:WritableCharacterChannel destinationChannel) {
    try {
        // Here is the string that is appended in-between.
        string intermediateCharacterString = " my name is ";
        // The first five characters in the source file contain the greeting.
        string greetingText = readCharacters(sourceChannel, 5);
        // This is a request for the next set of characters in the file.
        string name = readCharacters(sourceChannel, 15);
        // Here is how the greeting is written to the destination file.
        writeCharacters(destinationChannel, greetingText, 0);
        // Here is how the intermediate string is appended to the file.
        writeCharacters(destinationChannel, intermediateCharacterString, 0);
        // Here is how the remaining characters are written to the file,
        // leaving an offset.
        writeCharacters(destinationChannel, name, 1);
    } catch (error err) {
        throw err;
    }
}

public function main() {
    io:ReadableCharacterChannel sourceChannel = new (io:openReadableFile(
                                                         "./files/sample.txt"), "UTF-8");
    io:WritableCharacterChannel destinationChannel = new(io:openWritableFile(
                                                             "./files/sampleResponse.txt"), "UTF-8");
    try {
        io:println("Started to process the file.");
        process(sourceChannel, destinationChannel);
        io:println("File processing complete.");
    } catch (error err) {
        io:println("error occurred while processing chars " + err.message);
    } finally {
        // Close the created connections.
        match sourceChannel.close() {
            error sourceCloseError => {
                io:println("Error occured while closing the channel: " +
                            sourceCloseError.message);
            }
            () => {
                io:println("Source channel closed successfully.");
            }
        }
        match destinationChannel.close() {
            error destinationCloseError => {
                io:println("Error occured while closing the channel: " +
                            destinationCloseError.message);
            }
            () => {
                io:println("Destination channel closed successfully.");
            }
        }
    }
}

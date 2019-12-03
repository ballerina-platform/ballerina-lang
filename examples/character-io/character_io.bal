import ballerina/io;
import ballerina/log;

// This function reads content from a file,
// appends the additional `string`, and writes the content.
function process(io:ReadableCharacterChannel sc,
                 io:WritableCharacterChannel dc) returns error? {
    string intermediateCharacterString = " my name is ";
    // Reads the characters from the source channel.
    string greetingText = check sc.read(5);
    string name = check sc.read(15);
    // Writes the characters to the destination channel.
    var writeCharResult = check dc.write(greetingText, 0);
    var writeCharResult1 = check dc.write(intermediateCharacterString, 0);
    var writeCharResult2 = check dc.write(name, 1);
    return;
}

// Closes the readable character channel.
function closeRc(io:ReadableCharacterChannel ch) {
    var cr = ch.close();
    if (cr is error) {
        log:printError("Error occurred while closing the channel: ", err = cr);
    }
}

// Closes the writable character channel.
function closeWc(io:WritableCharacterChannel ch) {
    var cr = ch.close();
    if (cr is error) {
        log:printError("Error occurred while closing the channel: ", err = cr);
    }
}

public function main() returns error? {
    // This example uses the <BALLERINA_LANG>/examples/character-io/files/sample.txt file as the 
    // source file, which includes the text "Hello Ballerina!!". 
    // You can replace this with the file path of a preferred text file. 
    io:ReadableByteChannel readableFieldResult = check io:openReadableFile("./files/sample.txt");
    io:ReadableCharacterChannel sourceChannel =
            new(readableFieldResult, "UTF-8");

    // This example creates the <BALLERINA_LANG>/examples/character-io/files/sampleResponse.txt
    // destination file and writes the text "Hello my name is Ballerina!!"".  
    // You can replace this with the file path of a preferred text file.
    io:WritableByteChannel writableFileResult = check io:openWritableFile("./files/sampleResponse.txt");
    io:WritableCharacterChannel destinationChannel =
            new(writableFileResult, "UTF-8");
    io:println("Started to process the file.");
    // Processes the given `string`.
    var result = process(sourceChannel, destinationChannel);
    if (result is error) {
        log:printError("error occurred while processing chars ", err = result);
    } else {
        io:println("File processing complete.");
    }
    // Closes the readable channel.
    closeRc(sourceChannel);
    // Closes the writable channel.
    closeWc(destinationChannel);
}

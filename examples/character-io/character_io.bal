import ballerina/io;
import ballerina/log;

// This function reads content from a file,
// appends the additional string, and writes the content.
function process(io:ReadableCharacterChannel sc,
                 io:WritableCharacterChannel dc) returns error? {
    // Here is the string that is appended in-between.
    string intermediateCharacterString = " my name is ";
    // The first five characters in the source file contain the greeting.
    string greetingText = check sc.read(5);
    string name = check sc.read(15);
    var writeCharResult = check dc.write(greetingText, 0);
    var writeCharResult1 = check dc.write(intermediateCharacterString, 0);
    var writeCharResult2 = check dc.write(name, 1);
    return;
}

function closeRc(io:ReadableCharacterChannel ch) {
    var cr = ch.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}

function closeWc(io:WritableCharacterChannel ch) {
    var cr = ch.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}


public function main() {
    io:ReadableCharacterChannel sourceChannel =
            new(io:openReadableFile("./files/sample.txt"), "UTF-8");
    io:WritableCharacterChannel destinationChannel =
            new(io:openWritableFile("./files/sampleResponse.txt"), "UTF-8");
    io:println("Started to process the file.");
    var result = process(sourceChannel, destinationChannel);
    if (result is error) {
        log:printError("error occurred while processing chars ", err = result);
    } else {
        io:println("File processing complete.");
    }
    closeRc(sourceChannel);
    closeWc(destinationChannel);
}

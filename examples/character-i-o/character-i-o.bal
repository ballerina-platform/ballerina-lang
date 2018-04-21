import ballerina/io;


@Description {value:"This function returns a CharacterChannel from a given file location, according to the permissions and encoding that you specify."}
function getFileCharacterChannel (string filePath, io:Mode permission, string encoding) returns
io:CharacterChannel {

    // First, get the ByteChannel representation of the file.
    io:ByteChannel channel = io:openFile(filePath, permission);
    // Then, create an instance of the CharacterChannel from the ByteChannel to read content as text.
    io:CharacterChannel charChannel = new(channel, encoding);
    return charChannel;
}

@Description {value:"This function reads characters from 'channel', which is an instance of CharacterChannel."}
function readCharacters (io:CharacterChannel channel, int numberOfChars) returns string {

    //This is how the characters are read.
    var result = channel.read(numberOfChars);

    match result {
        string characters => {
            return characters;
         }
        error err => {
            throw err;
        }
    }
}

@Description {value:"This function wrties characters to 'channel'"}
function writeCharacters (io:CharacterChannel channel, string content, int startOffset) {
    //This is how the characters are written.
    var result = channel.write(content, startOffset);
    match result {
      int numberOfCharsWritten =>{
          io:println(" No of characters written : " + numberOfCharsWritten);
       }
      error err => {
          throw err;
      }
    }
}

@Description {value:"This function reads content from a file, appends the additional string, and writes the content."}
function process (io:CharacterChannel sourceChannel,
                  io:CharacterChannel destinationChannel) {
    string greetingText;
    string name;
    try {
        // Here is the string that is appended in-between.
        string intermediateCharacterString = " my name is ";
        // The first five characters in the source file contain the greeting.
        greetingText = readCharacters(sourceChannel, 5);
        // This is a request for the next set of characters in the file.
        name = readCharacters(sourceChannel, 15);
        // Here is how the greeting is written to the destination file.
        writeCharacters(destinationChannel, greetingText, 0);
        // Here is how the intermediate string is appended to the file.
        writeCharacters(destinationChannel, intermediateCharacterString, 0);
        // Here is how the remaining characters are written to the file, leaving an offset.
        writeCharacters(destinationChannel, name, 1);
    } catch (error err) {
        throw err;
    }
}

function main (string... args) {
    var sourceChannel = getFileCharacterChannel("./files/sample.txt", io:READ, "UTF-8");
    var destinationChannel = getFileCharacterChannel("./files/sampleResponse.txt", io:WRITE, "UTF-8");
    try {
        io:println("Started to process the file.");
        process(sourceChannel, destinationChannel);
        io:println("File processing complete.");
    } catch (error err) {
        io:println("error occurred while processing chars " + err.message);
    } finally {
        // Close the created connections.
        _ = sourceChannel.close();
        _ = destinationChannel.close();
    }
}

import ballerina/io;


@Description {value:"This function returns a CharacterChannel from a given file location according to the specified permissions and encoding."}
function getFileCharacterChannel (string filePath, string permission, string encoding) returns
io:CharacterChannel {

    // First we get the ByteChannel representation of the file.
    io:ByteChannel channel = io:openFile(filePath, permission);
    // Then we create a character channel from the byte channel to read content as text.
    var result = io:createCharacterChannel(channel, encoding);
    match result {
        io:CharacterChannel charChannel => {
            return charChannel;
        }
        io:IOError err => {
            throw err;
        }
    }
}

@Description {value:"This function reads characters from channel"}
function readCharacters (io:CharacterChannel channel, int numberOfChars) returns string {

    //This is how the characters are read.
    var result = channel.readCharacters(numberOfChars);

    match result {
        string characters => {
            return characters;
         }
        io:IOError err => {
            throw err;
        }
    }
}

@Description {value:"This function wrties characters to channel"}
function writeCharacters (io:CharacterChannel channel, string content, int startOffset) {
    //This is how the characters are written.
    var result = channel.writeCharacters(content, startOffset);
    match result {
      int numberOfCharsWritten =>{
          io:println(" No of characters written : " + numberOfCharsWritten);
       }
      io:IOError err => {
          throw err;
      }
    }
}

@Description {value:"This function reads content from a file, appends the additional string, and writes content."}
function process (io:CharacterChannel sourceChannel,
                  io:CharacterChannel destinationChannel) {
    string greetingText;
    string name;
    try {
        // Here is the string that is appended in-between.
        string intermediateCharacterString = " my name is ";
        // The first 5 characters in the source file contains the greeting.
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

function main (string[] args) {
    var sourceChannel = getFileCharacterChannel("./files/sample.txt", "r", "UTF-8");
    var destinationChannel = getFileCharacterChannel("./files/sampleResponse.txt", "w", "UTF-8");
    try {
        io:println("Started to process the file.");
        process(sourceChannel, destinationChannel);
        io:println("File processing complete.");
    } catch (error err) {
        io:println("error occurred while processing chars " + err.message);
    } finally {
        // Close the created connections.
        _ = sourceChannel.closeCharacterChannel();
        _ = destinationChannel.closeCharacterChannel();
    }
}

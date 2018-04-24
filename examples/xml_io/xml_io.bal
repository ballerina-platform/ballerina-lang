import ballerina/io;
import ballerina/log;

function close(io:CharacterChannel characterChannel){
    // Close the character channel when done
    characterChannel.close() but {
        error e => log:printError("Error occurred while closing character stream", err = e)
    };
}

function write(xml content,string path) {
    // From the given path a byte channel will be created
    io:ByteChannel byteChannel = io:openFile(path, io:WRITE);
    // Character channel will be derived from ByteChannel
    io:CharacterChannel characterChannel = new io:CharacterChannel(byteChannel, "UTF8");
    // This is how xml content could be written via the character channel
    match characterChannel.writeXml(content){
          error err =>{
            close(characterChannel);
            throw err;
          }
          () =>{
            close(characterChannel);
            io:println("Content written successfully");
          }
    }
}

function read(string path) returns xml {
    // From the given path a byte channel will be created
    io:ByteChannel byteChannel = io:openFile(path, io:READ);
    // Character channel will be derived from ByteChannel
    io:CharacterChannel characterChannel = new io:CharacterChannel(byteChannel, "UTF8");
    // This is how xml content could be read from the character channel
    match characterChannel.readXml(){
        xml result =>{
            close(characterChannel);
            return result;
        }
        error err =>{
            close(characterChannel);
            throw err;
        }
    }
}

function main(string... args) {
    string filePath = "./files/sample.xml";
    // We create a xml out of string
    xml x1 = xml `<book>The Lost World</book>`;
    io:println("Preparing to write xml file");
    // Content will be written
    write(x1,filePath);
    io:println("Preparing to read the content written");
    // Content will be read
    xml content = read(filePath);
    io:println(content);
}

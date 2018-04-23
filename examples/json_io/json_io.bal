import ballerina/io;
import ballerina/log;

function close(io:CharacterChannel characterChannel){
    //Close the character channel when done
    characterChannel.close() but {
        error e => log:printError("Error occurred while closing character stream", err = e)
    };
}

function write(json content,string path) {
    //From the given path a byte channel will be created
    io:ByteChannel byteChannel = io:openFile(path, io:WRITE);
    //Character channel will be derived from ByteChannel
    io:CharacterChannel characterChannel = new io:CharacterChannel(byteChannel, "UTF8");
    //This is how json content could be written via the character channel
    match characterChannel.writeJson(content){
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

function read(string path) returns json {
    //From the given path a byte channel will be created
    io:ByteChannel byteChannel = io:openFile("./files/sample.json", io:READ);
    //Character channel will be derived from ByteChannel
    io:CharacterChannel characterChannel = new io:CharacterChannel(byteChannel, "UTF8");
    //This is how json content could be read from the character channel
    match characterChannel.readJson(){
        json result =>{
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
    string filePath = "./files/sample.json";
    //We create a json out of string
    json j1 = {"Store":{
        "@id":"AST",
        "name":"Anne",
        "address":{
            "street":"Main",
            "city":"94"
        },
        "codes":["4", "8"]
    }
    };
    io:println("Preparing to write json file");
    //Content will be written
    write(j1,filePath);
    io:println("Preparing to read the content written");
    //Content will be read
    json content = read(filePath);
    io:println(content);
}

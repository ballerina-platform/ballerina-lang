import ballerina/io;

function main (string... args) {
    string filePath = "/test/path";
    string permission = "r";
    string chars = "data";

    io:ByteChannel bchannel = io:openFile(filePath, permission);
    var channelResult = io:createCharacterChannel(bchannel, "UTF-8");
    match channelResult {
        io:CharacterChannel channel => {
            var writeOutput = channel.writeCharacters(chars, 0);
            var readOutput = channel.readCharacters(1);
            match readOutput {
                string text => {
                    testFunction(text, text);
                }
                io:IOError ioError => return;
            }
        }
        io:IOError ioError => return;
    }
}

public function testFunction (string sensitiveValue, string anyValue) {

}


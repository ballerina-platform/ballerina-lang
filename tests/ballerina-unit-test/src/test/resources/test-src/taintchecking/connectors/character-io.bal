import ballerina/io;

function main (string... args) {
    string filePath = "/test/path";
    io:Mode permission = "r";
    string chars = "data";

    io:ByteChannel bchannel = io:openFile(filePath, permission);
    io:CharacterChannel channel = new io:CharacterChannel(bchannel, "UTF-8");

    var writeOutput = channel.write(chars, 0);
    var readOutput = channel.read(1);
    match readOutput {
        string text => {
            testFunction(text, text);
        }
        error ioError => return;
    }
}

public function testFunction (string sensitiveValue, string anyValue) {

}


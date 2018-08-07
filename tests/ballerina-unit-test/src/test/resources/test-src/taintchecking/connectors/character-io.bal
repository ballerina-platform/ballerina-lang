import ballerina/io;

function main (string... args) {
    string filePath = "/test/path";
    io:Mode permission = "r";
    string chars = "data";

    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    io:CharacterChannel charChannel = new io:CharacterChannel(byteChannel, "UTF-8");

    var writeOutput = charChannel.write(chars, 0);
    var readOutput = charChannel.read(1);
    match readOutput {
        string text => {
            testFunction(text, text);
        }
        error ioError => return;
    }
}

public function testFunction (string sensitiveValue, string anyValue) {

}


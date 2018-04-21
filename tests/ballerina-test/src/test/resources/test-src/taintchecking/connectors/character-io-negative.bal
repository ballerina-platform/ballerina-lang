import ballerina/io;

function main (string... args) {
    string filePath = args[0];
    io:Mode permission = args[0];
    string chars = args[0];

    int intArg = check <int> args[0];

    io:ByteChannel bchannel = io:openFile(filePath, permission);
    io:CharacterChannel channel = new io:CharacterChannel(bchannel, "UTF-8");
    var writeOutput = channel.write(chars, 0);
    var readOutput = channel.read(intArg);
    match readOutput {
        string text => {
            testFunction(text, text);
        }
        error ioError => return;
    }
}

public function testFunction (@sensitive string sensitiveValue, string anyValue) {

}


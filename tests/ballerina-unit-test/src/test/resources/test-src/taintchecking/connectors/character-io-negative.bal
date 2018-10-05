import ballerina/io;

public function main (string... args) {
    string filePath = args[0];
    io:Mode permission = "r";
    string chars = args[0];

    int intArg = check <int> args[0];

    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    io:CharacterChannel charChannel = new io:CharacterChannel(byteChannel, "UTF-8");
    var writeOutput = charChannel.write(chars, 0);
    var readOutput = charChannel.read(intArg);
    match readOutput {
        string text => {
            testFunction(text, text);
        }
        error ioError => return;
    }
}

public function testFunction (@sensitive string sensitiveValue, string anyValue) {

}


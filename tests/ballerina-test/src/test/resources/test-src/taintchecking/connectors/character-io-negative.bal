import ballerina/io;

function main (string[] args) {
    string filePath = args[0];
    string permission = args[0];
    string chars = args[0];

    int intArg = check <int> args[0];

    io:ByteChannel bchannel = io:openFile(filePath, permission);
    var channelResult = io:createCharacterChannel(bchannel, "UTF-8");
    match channelResult {
        io:CharacterChannel channel => {
            var writeOutput = channel.writeCharacters(chars, 0);
            var readOutput = channel.readCharacters(intArg);
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

public function testFunction (@sensitive string sensitiveValue, string anyValue) {

}


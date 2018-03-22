import ballerina/io;

function main (string[] args) {
    string filePath = "/test/path";
    string permission = "r";
    string chars = "data";

    io:ByteChannel bchannel = io:openFile(filePath, permission);
    io:CharacterChannel channel;
    channel, _ = io:createCharacterChannel(bchannel, "UTF-8");

    int len;
    len, _ = channel.writeCharacters(chars, 0);

    io:IOError err;
    string text;
    text, err = channel.readCharacters(1);

    testFunction(text, text);
}

public function testFunction (string sensitiveValue, string anyValue) {

}

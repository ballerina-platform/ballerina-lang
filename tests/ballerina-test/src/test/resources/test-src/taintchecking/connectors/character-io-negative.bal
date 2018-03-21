import ballerina/io;

function main (string[] args) {
    string filePath = args[0];
    string permission = args[0];
    string chars = args[0];

    io:ByteChannel bchannel = io:openFile(filePath, permission);
    io:CharacterChannel channel;
    channel, _ = io:createCharacterChannel(bchannel, "UTF-8");

    int len;
    len, _ = channel.writeCharacters(chars, 0);

    io:IOError err;
    string text;

    int readLen;
    readLen, _ = <int>args[0];
    text, err = channel.readCharacters(readLen);

    testFunction(text, text);
}

public function testFunction (@sensitive string sensitiveValue, string anyValue) {

}

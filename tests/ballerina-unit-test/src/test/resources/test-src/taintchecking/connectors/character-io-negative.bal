import ballerina/io;

public function main (string... args) {
    string filePath = args[0];
    string chars = args[0];

    int intArg = check <int> args[0];

    io:ReadableByteChannel rbh = io:openReadableFile(filePath);
    io:ReadableCharacterChannel rch = new io:ReadableCharacterChannel(rbh, "UTF-8");

    io:WritableByteChannel wbh = io:openWritableFile(filePath);
    io:WritableCharacterChannel wch = new io:WritableCharacterChannel(wbh, "UTF-8");

    var writeOutput = wch.write(chars, 0);
    var readOutput = rch.read(intArg);
    match readOutput {
        string text => {
            testFunction(text, text);
        }
        error ioError => return;
    }
}

public function testFunction (@sensitive string sensitiveValue, string anyValue) {

}


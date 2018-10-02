import ballerina/io;

public function main (string... args) {
    string filePath = "/test/path";
    string chars = "data";

    io:ReadableByteChannel rbh = io:openReadableFile(filePath);
    io:ReadableCharacterChannel rch = new io:ReadableCharacterChannel(rbh, "UTF-8");

    io:WritableByteChannel wbh = io:openWritableFile(filePath);
    io:WritableCharacterChannel wch = new io:WritableCharacterChannel(wbh, "UTF-8");

    var writeOutput = wch.write(chars, 0);
    var readOutput = rch.read(1);
    match readOutput {
        string text => {
            testFunction(text, text);
        }
        error ioError => return;
    }
}

public function testFunction (string sensitiveValue, string anyValue) {

}


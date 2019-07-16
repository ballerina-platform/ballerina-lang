import ballerina/io;

public function main (string... args) {
    string filePath = "/test/path";
    string chars = "data";

    io:ReadableByteChannel rbh = checkpanic io:openReadableFile(filePath);
    io:ReadableCharacterChannel rch = new io:ReadableCharacterChannel(rbh, "UTF-8");

    io:WritableByteChannel wbh = checkpanic io:openWritableFile(filePath);
    io:WritableCharacterChannel wch = new io:WritableCharacterChannel(wbh, "UTF-8");

    var writeOutput = wch.write(chars, 0);
    var readOutput = rch.read(1);
    if (readOutput is string) {
        testFunction(readOutput, readOutput);
    } else {
        error err = readOutput;
        panic err;
    }
}

public function testFunction (string sensitiveValue, string anyValue) {

}


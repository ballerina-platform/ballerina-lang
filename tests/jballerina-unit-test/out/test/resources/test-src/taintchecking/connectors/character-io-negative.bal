import ballerina/io;
import ballerina/lang.'int as ints;

public function main (string... args) returns error? {
    string filePath = args[0];
    string chars = args[0];

    var intArg = ints:fromString(args[0]);
    if (intArg is int) {
        io:ReadableByteChannel rbh = checkpanic io:openReadableFile(filePath);
        io:ReadableCharacterChannel rch = new io:ReadableCharacterChannel(rbh, "UTF-8");

        io:WritableByteChannel wbh = checkpanic io:openWritableFile(filePath);
        io:WritableCharacterChannel wch = new io:WritableCharacterChannel(wbh, "UTF-8");

        var writeOutput = wch.write(chars, 0);
        var readOutput = rch.read(intArg);
        if (readOutput is string) {
            testFunction(readOutput, readOutput);
        } else {
            error err = readOutput;
            panic err;
        }
    } else {
        panic intArg;
    }
    return ();
}

public function testFunction (@untainted string untaintedValue, string anyValue) {

}


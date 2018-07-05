import ballerina/io;
import ballerina/file;

function main (string... args) {
    file:Path target = new("/tmp/result.txt");
    string absolutePath = target.toAbsolutePath().getPathValue();
    io:ByteChannel bchannel = io:openFile(absolutePath, args[0]);
    int intArg = check <int> args[0];
    
    var readOutput = bchannel.read(intArg);
    match readOutput {
        (blob,int) data => {
            blob readData;
            int readLen;
            (readData, readLen) = data;
            testFunction(readData, readData);
        }
        io:IOError ioError => return;
    }
}

public function testFunction (@sensitive any sensitiveValue, any anyValue) {

}


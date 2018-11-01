import ballerina/io;
import ballerina/file;

public function main (string... args) {
    file:Path target = new("/tmp/result.txt");
    string absolutePath = target.toAbsolutePath().getPathValue();
    io:ReadableByteChannel byteChannel = io:openReadableFile(absolutePath);
    int intArg = check <int> args[0];
    
    var readOutput = byteChannel.read(intArg);
    match readOutput {
        (blob,int) data => {
            byte[] readData;
            int readLen;
            (readData, readLen) = data;
            testFunction(readData, readData);
        }
        io:IOError ioError => return;
    }
}

public function testFunction (@sensitive any sensitiveValue, any anyValue) {

}


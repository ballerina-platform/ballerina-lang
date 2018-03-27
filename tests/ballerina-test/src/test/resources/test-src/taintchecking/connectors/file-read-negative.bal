import ballerina/io;
import ballerina/file;

function main (string[] args) {
    file:File target = {path:"/tmp/result.txt"};
    target.open(args[0]);

    io:ByteChannel bchannel = target.openChannel(args[0]);
    int intArg =? <int> args[0];
    
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


import ballerina/io;
import ballerina/file;

function main (string[] args) {
    file:File target = {path:"/tmp/result.txt"};
    target.open(args[0]);

    io:ByteChannel bchannel = target.openChannel(args[0]);
    int intArg;
    intArg, _ = <int> args[0];

    blob data;
    int len;
    data, len, _ = bchannel.read(intArg);

    testFunction(data, data);
}

public function testFunction (@sensitive any sensitiveValue, any anyValue) {

}

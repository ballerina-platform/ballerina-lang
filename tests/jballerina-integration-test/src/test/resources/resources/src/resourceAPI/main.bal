import ballerina/io;
import ballerina/log;

function closeRc(io:ReadableCharacterChannel ch) {
    var cr = ch.close();
    if (cr is error) {
        log:printError("Error occurred while closing the channel: ", err = cr);
    }
}

function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    abstract object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occurred while closing the channel: ", cr);
    }
}

public function main() returns @tainted error? {
    io:ReadableByteChannel readableFieldResult = check io:getResource("abc/pqr/resource.txt");
    io:ReadableCharacterChannel sourceChannel = new(readableFieldResult, "UTF-8");
    io:println(check sourceChannel.read(1000));
    closeRc(sourceChannel);
    close(readableFieldResult);

    string[] readDirResults = io:getResources();
    foreach var item in readDirResults {
        io:println(item);
    }
}

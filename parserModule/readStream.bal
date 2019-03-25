import ballerina/io;
import ballerina/log;

public function main() {
    io:ReadableCharacterChannel sourceChannel = new(io:openReadableFile("inputFile.txt"), "UTF-8");
    io:println("Started to process the file.");
    BufferReader|error bReader = trap new BufferReader(capacity = 5, sourceChannel);

    if (bReader is error) {
        log:printError("error occurred while processing chars ", err = bReader);
    } else {
        Lexer lex = new(bReader);
        ParserBufferReader pBuffer = new(lex, capacity = 5);
        //pBuffer.printTokenBuffer();
        Parser parser = new(pBuffer);
        PackageNode pkgNode = parser.parse();
        io:println(pkgNode);
    }
    closeRc(sourceChannel);
}
function closeRc(io:ReadableCharacterChannel ch) {
    var cr = ch.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}


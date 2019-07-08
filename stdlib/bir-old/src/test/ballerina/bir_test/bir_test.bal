import ballerina/bir;
import ballerina/io;

public function main(string... args) {
}

function testParseType(byte[] cpBinary, byte[] birBinary) returns string {
    io:ReadableByteChannel byteChannel = io:createReadableChannel(birBinary);
    bir:ChannelReader reader = new(byteChannel);
    var cpParser = parseCp(cpBinary);
    bir:BirChannelReader cpAwareParser = new(reader, cpParser.parse());
    bir:TypeParser typeParser = new(reader, cpParser);
    return  bir:serialize(typeParser.parseType());
}

function parseCp(byte[] cpBinary) returns bir:ConstPoolParser {
    io:ReadableByteChannel cpByteChannel = io:createReadableChannel(cpBinary);
    bir:ChannelReader reader = new(cpByteChannel);
    bir:ConstPoolParser cpParser = new(reader);
    return cpParser;
}

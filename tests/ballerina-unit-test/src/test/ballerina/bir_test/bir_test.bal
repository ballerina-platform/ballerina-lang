import ballerina/bir;
import ballerina/io;

public function main(string... args) {
}

function testParseType(byte[] cpBinary, byte[] birBinary) returns string {
    io:ByteChannel byteChannel = io:createMemoryChannel(birBinary);
    bir:ChannelReader reader = new(byteChannel);
    var cp = parseCp(cpBinary);
    bir:BirChannelReader cpAwareParser = new(reader, cp);
    bir:TypeParser typeParser = new(cpAwareParser);
    return  bir:serialize(typeParser.parseType());
}

function parseCp(byte[] cpBinary) returns bir:ConstPool {
    io:ByteChannel cpByteChannel = io:createMemoryChannel(cpBinary);
    bir:ChannelReader reader = new(cpByteChannel);
    bir:ConstPoolParser cpParser = new(reader);
    return cpParser.parse();

}

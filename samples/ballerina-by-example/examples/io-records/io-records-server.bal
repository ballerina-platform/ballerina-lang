import ballerina.tcp;
import ballerina.io;
import ballerina.lang.system;


function getRecordChannel (io:ByteChannel channel, string rs, string fs) (io:TextRecordChannel) {
    io:CharacterChannel characterChannel = io:toCharacterChannel(channel, "UTF-8");
    io:TextRecordChannel recordChannel = io:toTextRecordChannel(characterChannel, rs, fs);
    return recordChannel;
}


function main (string[] args) {

    tcp:Socket serverSocketAddress = {address:"localhost", port:8989};
    io:ByteChannel dstByteChannel = tcp:openChannel(serverSocketAddress, "r");
    io:TextRecordChannel dstTxtRecordChannel = getRecordChannel(dstByteChannel, "\n", "\\|");

    int recordLength = -1;

    while(recordLength != 0) {
        string[] records = io:readTextRecord(dstTxtRecordChannel);
        recordLength = lengthof records;
        system:println(records);
    }

}

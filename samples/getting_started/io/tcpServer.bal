import ballerina.tcp;
import ballerina.io;
import ballerina.lang.system;


function getRecordChannel (io:ByteChannel channel, string rs, string fs) (io:TextRecordChannel) {
    io:CharacterChannel characterChannel = channel.toCharacterChannel( "UTF-8");
    io:TextRecordChannel recordChannel = characterChannel.toTextRecordChannel(rs, fs);
    return recordChannel;
}


function main (string[] args) {

    system:println("Starting TCP Server...");

    tcp:Socket serverSocketAddress = {address:"localhost", port:8989};
    io:ByteChannel dstByteChannel = serverSocketAddress.openChannel("r");
    io:TextRecordChannel dstTxtRecordChannel = getRecordChannel(dstByteChannel, "\n", "\\|");

    int recordLength = -1;

    while(recordLength != 0) {
        string[] records = dstTxtRecordChannel.readTextRecord();
        recordLength = lengthof records;
        system:println(records);
    }

    system:println("Shutting Down TCP Server...");

}

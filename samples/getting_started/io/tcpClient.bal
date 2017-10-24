import ballerina.tcp;
import ballerina.io;
import ballerina.lang.files;
import ballerina.lang.system;


function getRecordChannel (io:ByteChannel channel, string rs, string fs) (io:TextRecordChannel) {
    io:CharacterChannel characterChannel = channel.toCharacterChannel("UTF-8");
    io:TextRecordChannel recordChannel = characterChannel.toTextRecordChannel(rs, fs);
    return recordChannel;
}


function main (string[] args) {

    files:File srcFilePath = {path:"../samples/io/files/sample.csv"};
    tcp:Socket dstSocketAddress = {address:"localhost", port:8989};

    io:ByteChannel srcByteChannel = srcFilePath.openChannel("r");
    io:ByteChannel dstByteChannel = dstSocketAddress.openChannel("w");

    io:TextRecordChannel srcTxtRecordChannel = getRecordChannel(srcByteChannel, "\\r?\\n", ",");
    io:TextRecordChannel dstTxtRecordChannel = getRecordChannel(dstByteChannel, "\n", "|");

    int numberOfFields = -1;

    system:println("Sending Records...");

    while (numberOfFields != 0) {
        string [] records = srcTxtRecordChannel.readTextRecord();
        dstTxtRecordChannel.writeTextRecord(records);
        numberOfFields = lengthof records;
    }

    system:println("Record Processing Complete!");

}

import ballerina.tcp;
import ballerina.io;
import ballerina.lang.files;


function getRecordChannel (io:ByteChannel channel, string rs, string fs) (io:TextRecordChannel) {
    io:CharacterChannel characterChannel = io:toCharacterChannel(channel, "UTF-8");
    io:TextRecordChannel recordChannel = io:toTextRecordChannel(characterChannel, rs, fs);
    return recordChannel;
}


function main (string[] args) {

    files:File srcFilePath = {path:"./files/sample.csv"};
    tcp:Socket dstSocketAddress = {address:"localhost", port:8989};

    io:ByteChannel srcByteChannel = files:openChannel(srcFilePath, "r");
    io:ByteChannel dstByteChannel = tcp:openChannel(dstSocketAddress, "w");

    io:TextRecordChannel srcTxtRecordChannel = getRecordChannel(srcByteChannel, "\\r?\\n", ",");
    io:TextRecordChannel dstTxtRecordChannel = getRecordChannel(dstByteChannel, "\n", "|");

    int numberOfFields = -1;

    while (numberOfFields != 0) {
        string [] records = io:readTextRecord(srcTxtRecordChannel);
        io:writeTextRecord(dstTxtRecordChannel, records);
        numberOfFields = lengthof records;
    }

}

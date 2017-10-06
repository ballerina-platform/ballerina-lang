import ballerina.tcp;
import ballerina.io;
import ballerina.lang.files;

function getFileChannel (string filePath, string permission) (io:ByteChannel) {
    files:File src = {path:filePath};
    files:openAsync(src, permission);
    io:ByteChannel channel = io:toByteChannel(src);
    return channel;
}

function getRecordChannel (io:ByteChannel channel, string rs, string fs) (io:TextRecordChannel) {
    io:CharacterChannel characterChannel = io:toCharacterChannel(channel, "UTF-8");
    io:TextRecordChannel recordChannel = io:toTextRecordChannel(characterChannel, rs, fs);
    return recordChannel;
}


function main (string[] args) {
    io:ByteChannel srcByteChannel = getFileChannel("/tmp/sample.csv", "r");
    io:ByteChannel dstByteChannel = tcp:openChannel("localhost", 8989);

    io:TextRecordChannel srcTxtRecordChannel = getRecordChannel(srcByteChannel, "\\r?\\n", ",");
    io:TextRecordChannel dstTxtRecordChannel = getRecordChannel(dstByteChannel, "\n", "|");

    string[] records = io:readTextRecord(srcTxtRecordChannel);
    int numberOfFields = lengthof records;

    while (numberOfFields > 0) {
        io:writeTextRecord(dstTxtRecordChannel, records);
        records = io:readTextRecord(srcTxtRecordChannel);
        numberOfFields = lengthof records;
    }

}

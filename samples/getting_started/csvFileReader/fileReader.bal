import ballerina.lang.files;
import ballerina.io;

function getFileRecordChannel (string filePath, string permission, string encoding, string rs, string fs) (io:TextRecordChannel) {
    files:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
    io:CharacterChannel characterChannel = channel.toCharacterChannel(encoding);
    io:TextRecordChannel textRecordChannel = characterChannel.toTextRecordChannel(rs, fs);
    return textRecordChannel;
}

function process (io:TextRecordChannel srcRecordChannel, io:TextRecordChannel dstRecordChannel) {
    int numberOfFields = -1;
    while (numberOfFields != 0) {
        string[] records = srcRecordChannel.readTextRecord();
        dstRecordChannel.writeTextRecord(records);
        numberOfFields = lengthof records;
    }
}

function main (string[] args) {
    string srcFileName = "../samples/csvFileReader/files/sample.csv";
    string dstFileName = "../samples/csvFileReader/files/sampleResponse.txt";
    io:TextRecordChannel srcRecordChannel = getFileRecordChannel(srcFileName, "r", "UTF-8", "\\r?\\n", ",");
    io:TextRecordChannel dstRecordChannel = getFileRecordChannel(dstFileName, "w", "UTF-8", "\n", "|");
    process(srcRecordChannel, dstRecordChannel);
}

import ballerina.io;

function getFileRecordChannel (string filePath, string permission, string encoding, string rs, string fs) (io:TextRecordChannel) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    io:CharacterChannel characterChannel = io:createCharacterChannel(channel, encoding);
    io:TextRecordChannel textRecordChannel = characterChannel.toTextRecordChannel(rs, fs);
    return textRecordChannel;
}

function process (io:TextRecordChannel srcRecordChannel, io:TextRecordChannel dstRecordChannel) {
    while (srcRecordChannel.hasNextTextRecord()) {
        string[] records = srcRecordChannel.nextTextRecord();
        dstRecordChannel.writeTextRecord(records);
    }
}

function main (string[] args) {
    string srcFileName = "../samples/csvFileProcessor/files/sample.csv";
    string dstFileName = "../samples/csvFileProcessor/files/sampleResponse.txt";
    io:TextRecordChannel srcRecordChannel = getFileRecordChannel(srcFileName, "r", "UTF-8", "\\r?\\n", ",");
    io:TextRecordChannel dstRecordChannel = getFileRecordChannel(dstFileName, "w", "UTF-8", "\n", "|");
    process(srcRecordChannel, dstRecordChannel);
    srcRecordChannel.closeTextRecordChannel();
    dstRecordChannel.closeTextRecordChannel();
}
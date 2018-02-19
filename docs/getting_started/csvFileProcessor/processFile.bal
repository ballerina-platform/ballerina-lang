import ballerina.io;

function getFileRecordChannel (string filePath, string permission, string encoding, string rs, string fs) (io:DelimitedRecordChannel) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    io:CharacterChannel characterChannel = channel.toCharacterChannel(encoding);
    io:DelimitedRecordChannel textRecordChannel = characterChannel.toTextRecordChannel(rs, fs);
    return textRecordChannel;
}

function process (io:DelimitedRecordChannel srcRecordChannel, io:DelimitedRecordChannel dstRecordChannel) {
    while (srcRecordChannel.hasNextTextRecord()) {
        string[] records = srcRecordChannel.nextTextRecord();
        dstRecordChannel.writeTextRecord(records);
    }
}

function main (string[] args) {
    string srcFileName = "../samples/csvFileProcessor/files/sample.csv";
    string dstFileName = "../samples/csvFileProcessor/files/sampleResponse.txt";
    io:DelimitedRecordChannel srcRecordChannel = getFileRecordChannel(srcFileName, "r", "UTF-8", "\\r?\\n", ",");
    io:DelimitedRecordChannel dstRecordChannel = getFileRecordChannel(dstFileName, "w", "UTF-8", "\n", "|");
    process(srcRecordChannel, dstRecordChannel);
    srcRecordChannel.closeTextRecordChannel();
    dstRecordChannel.closeTextRecordChannel();
}
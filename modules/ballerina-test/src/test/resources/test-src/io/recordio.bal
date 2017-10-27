import ballerina.file;
import ballerina.io;

io:TextRecordChannel textRecordChannel;

function initFileChannel(string filePath,string permission,string encoding,string rs,string fs){
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
    io:CharacterChannel  characterChannel = channel.toCharacterChannel(encoding);
    textRecordChannel = characterChannel.toTextRecordChannel(rs,fs);
}

function readRecord () (string[]) {
    string[] fields = textRecordChannel.readTextRecord();
    return fields;
}

function writeRecord (string[] fields) {
    textRecordChannel.writeTextRecord(fields);
}

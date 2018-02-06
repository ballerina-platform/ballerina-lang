import ballerina.file;
import ballerina.io;

io:TextRecordChannel textRecordChannel;

function initFileReadChannel (string filePath, string encoding, string rs, string fs) {
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(file:AccessMode.R);
    io:CharacterChannel  characterChannel = channel.toCharacterChannel(encoding);
    textRecordChannel = characterChannel.toTextRecordChannel(rs,fs);
}

function initFileWriteChannel (string filePath, string encoding, string rs, string fs) {
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(file:AccessMode.W);
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

function close(){
    textRecordChannel.closeTextRecordChannel();
}

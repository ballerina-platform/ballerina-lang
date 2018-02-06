import ballerina.file;
import ballerina.io;

io:DelimitedRecordChannel delimitedRecordChannel;

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
    delimitedRecordChannel = characterChannel.toTextRecordChannel(rs, fs);
}

function nextRecord () (string[]) {
    string[] fields = delimitedRecordChannel.nextTextRecord();
    return fields;
}

function writeRecord (string[] fields) {
    delimitedRecordChannel.writeTextRecord(fields);
}

function close(){
    delimitedRecordChannel.closeDelimitedRecordChannel();
}

function hasNextRecord () (boolean) {
    return delimitedRecordChannel.hasNextTextRecord();
}

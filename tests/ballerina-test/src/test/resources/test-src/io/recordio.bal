import ballerina.file;
import ballerina.io;

io:DelimitedRecordChannel delimitedRecordChannel;

function initFileChannel(string filePath,string permission,string encoding,string rs,string fs){
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
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

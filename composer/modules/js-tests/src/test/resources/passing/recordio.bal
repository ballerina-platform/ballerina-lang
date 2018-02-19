import ballerina.io;

io:TextRecordChannel textRecordChannel;

function initFileChannel(string filePath,string permission,string encoding,string rs,string fs){
    io:ByteChannel channel = io:openFile(filePath, permission);
    io:CharacterChannel characterChannel = io:createCharacterChannel(channel, encoding);
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

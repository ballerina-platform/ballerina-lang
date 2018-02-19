import ballerina.io;

io:TextRecordChannel textRecordChannel;

function initFileChannel(string filePath,string permission,string encoding,string rs,string fs){
    io:ByteChannel channel = io:openFile(filePath, permission);
    io:CharacterChannel characterChannel = io:createCharacterChannel(channel, encoding);
    textRecordChannel = characterChannel.toTextRecordChannel(rs,fs);
}

function nextRecord () (string[]) {
    string[] fields = textRecordChannel.nextTextRecord();
    return fields;
}

function writeRecord (string[] fields) {
    textRecordChannel.writeTextRecord(fields);
}

function close(){
    textRecordChannel.closeTextRecordChannel();
}

function hasNextRecord () (boolean) {
    return textRecordChannel.hasNextTextRecord();
}
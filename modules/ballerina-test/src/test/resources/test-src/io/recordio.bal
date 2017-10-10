import ballerina.lang.files;
import ballerina.io;

io:TextRecordChannel textRecordChannel;

function initFileChannel(string filePath,string permission,string encoding,string rs,string fs){
    files:File src = {path:filePath};
    io:ByteChannel channel = files:openChannel(src, permission);
    io:CharacterChannel  characterChannel = io:toCharacterChannel(channel,encoding);
    textRecordChannel = io:toTextRecordChannel(characterChannel,rs,fs);
}

function readRecord () (string[]) {
    string[] fields = io:readTextRecord(textRecordChannel);
    return fields;
}

function writeRecord (string[] fields) {
    io:writeTextRecord(textRecordChannel, fields);
}
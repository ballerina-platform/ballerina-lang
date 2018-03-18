import ballerina.io;

struct Employee {
    string id;
    string name;
    float salary;
}

io:DelimitedRecordChannel delimitedRecordChannel;

function initFileChannel(string filePath,string permission,string encoding,string rs,string fs){
    io:ByteChannel channel = io:openFile(filePath, permission);
    io:CharacterChannel characterChannel;
    characterChannel, _ = io:createCharacterChannel(channel, encoding);
    delimitedRecordChannel, _ = io:createDelimitedRecordChannel(characterChannel, rs, fs);
}

function nextRecord () (string[]) {
    string[] fields;
    fields, _ = delimitedRecordChannel.nextTextRecord();
    return fields;
}

function writeRecord (string[] fields) {
   _ = delimitedRecordChannel.writeTextRecord(fields);
}

function close(){
    _ = delimitedRecordChannel.closeDelimitedRecordChannel();
}

function hasNextRecord () (boolean) {
    return delimitedRecordChannel.hasNextTextRecord();
}

function loadToTable (string filePath) (float total) {
    table <Employee> tb;
    tb, _ = io:loadToTable(filePath, "\n", ",", "UTF-8", false, typeof Employee);
    foreach x in tb {
        total = total + x.salary;
    }
    return;
}

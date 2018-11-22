import ballerina/io;

io:ReadableTextRecordChannel? rch = ();
io:WritableTextRecordChannel? wch = ();

function initReadableChannel(string filePath, string encoding, string recordSeperator,
                                    string fieldSeperator) {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
    rch = untaint new io:ReadableTextRecordChannel(charChannel, fs = fieldSeperator, rs = recordSeperator);
}

function initWritableChannel(string filePath, string encoding, string recordSeperator,
                             string fieldSeperator) {
    io:WritableByteChannel byteChannel = io:openWritableFile(filePath);
    io:WritableCharacterChannel charChannel = new io:WritableCharacterChannel(byteChannel, encoding);
    wch = untaint new io:WritableTextRecordChannel(charChannel, fs = fieldSeperator, rs = recordSeperator);
}


function nextRecord() returns (string[]|error) {
    var result = rch.getNext();
    if (result is string[]) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error("Record channel not initialized properly");
        return e;
    }
}

function writeRecord(string[] fields) {
    var result = wch.write(fields);
}

function closeReadableChannel() {
    var err = rch.close();
}

function closeWritableChannel() {
    var err = wch.close();
}


function hasNextRecord() returns boolean? {
    return rch.hasNext();
}

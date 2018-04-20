import ballerina/io;

io:CSVChannel? csvChannel;

function initCSVChannel(string filePath, io:Mode permission, string encoding, io:Seperator fieldSeperator) {
    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    io:CharacterChannel charChannel = new io:CharacterChannel(byteChannel, encoding);
    csvChannel = new io:CSVChannel(charChannel, fs = fieldSeperator);
}

function initOpenCsv(string filePath, io:Mode permission, string encoding, io:Seperator fs) {
    csvChannel =check io:openCsvFile(filePath, mode=permission, fieldSeperator=fs,charset=encoding);
}

function nextRecord() returns (string[]|error) {
    var result = csvChannel.getNext();
    match result {
        string[] fields => {
            return fields;
        }
        error err => {
            return err;
        }
        () => {
            error e = {message: "Record channel not initialized properly"};
            return e;
        }
    }
}

function writeRecord(string[] fields) {
    var result = csvChannel.write(fields);
}

function close() {
    var err = csvChannel.close();
}


function hasNextRecord() returns boolean? {
    return csvChannel.hasNext();
}

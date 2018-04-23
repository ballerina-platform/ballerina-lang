import ballerina/io;

io:DelimitedTextRecordChannel? txtChannel;

function initDelimitedRecordChannel(string filePath, io:Mode permission, string encoding, string recordSeperator,
                                    string fieldSeperator) {
    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    io:CharacterChannel charChannel = new io:CharacterChannel(byteChannel, encoding);
    txtChannel = new io:DelimitedTextRecordChannel(charChannel, fs = fieldSeperator, rs = recordSeperator);
}

function nextRecord() returns (string[]|error) {
    var result = txtChannel.getNext();
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
    var result = txtChannel.write(fields);
}

function close() {
    var err = txtChannel.close();
}


function hasNextRecord() returns boolean? {
    return txtChannel.hasNext();
}

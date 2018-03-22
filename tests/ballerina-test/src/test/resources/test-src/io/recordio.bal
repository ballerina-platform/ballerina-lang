import ballerina/io;

struct Employee {
    string id;
    string name;
    float salary;
}

io:DelimitedRecordChannel|null txtChannel;

function initDelimitedRecordChannel (string filePath, string permission, string encoding, string rs, string fs) {
    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    var characterChannelResult = io:createCharacterChannel(byteChannel, encoding);
    match characterChannelResult {
        io:CharacterChannel ch => {
            var delimitedRecordChannelResult = io:createDelimitedRecordChannel(ch, rs, fs);
            match delimitedRecordChannelResult {
                io:DelimitedRecordChannel recordChannel => {
                    txtChannel = recordChannel;
                }
                io:IOError err => {
                    throw err;
                }
            }
        }
        io:IOError err => {
            io:println("Error occurred in record channel");
            throw err;
        }
    }
}

function nextRecord () returns (string[]) {
    string[] empty = [];
    match txtChannel {
        io:DelimitedRecordChannel delimChannel => {
            var result = delimChannel.nextTextRecord();
            match result {
                string[] fields => {
                    return fields;
                }
                io:IOError err => {
                    throw err;
                }
            }
            return empty;
        }
        (any|null) => {
            return empty;
        }

    }
}

function writeRecord (string[] fields) {
    match txtChannel {
        io:DelimitedRecordChannel delimChannel => {
            var result = delimChannel.writeTextRecord(fields);
        }
        (any|null) => {
            io:println("Feilds cannot be written");
        }
    }
}

function close () {
    match txtChannel {
        io:DelimitedRecordChannel delimChannel => {
            var err = delimChannel.closeDelimitedRecordChannel();
        }
        (any|null) => {
            io:println("Channel cannot be closed");
        }
    }
}


function hasNextRecord () returns (boolean) {
    boolean hasNext;
    match txtChannel {
        io:DelimitedRecordChannel delimChannel => {
            hasNext = delimChannel.hasNextTextRecord();
            return hasNext;
        }
        (any|null) => {
            io:println("Channel cannot be closed");
            return hasNext;
        }
    }

}

function loadToTable (string filePath) returns (float) {
    float total;
    var result = io:loadToTable(filePath, "\n", ",", "UTF-8", false, typeof Employee);
    match result {
        table <Employee> tb => {
            foreach x in tb {
                total = total + x.salary;
            }
            return total;
        }
        io:IOError err => {
            throw err;
        }
    }
    return -1;
}

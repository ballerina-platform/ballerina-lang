import ballerina/io;

struct Employee {
    string id;
    string name;
    float salary;
}

io:DelimitedRecordChannel|null txtChannel;

function initDelimitedRecordChannel (string filePath, string permission, string encoding, string rs, string fs)
returns (boolean|io:IOError){
    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    var characterChannelResult = io:createCharacterChannel(byteChannel, encoding);
    match characterChannelResult {
        io:CharacterChannel ch =>{
            var delimitedRecordChannelResult = io:createDelimitedRecordChannel(ch, rs, fs);
            match delimitedRecordChannelResult {
                io:DelimitedRecordChannel recordChannel =>{
                    txtChannel = recordChannel;
                    return true;
                }
                io:IOError err =>{
                    return err;
                }
            }
        }
        io:IOError err =>{
            io:println("Error occurred in record channel");
            return err;
        }
    }
}

function nextRecord () returns (string[]|io:IOError) {
    string[] empty = [];
    match txtChannel {
        io:DelimitedRecordChannel delimChannel =>{
            var result = delimChannel.nextTextRecord();
            match result {
                string[] fields =>{
                    return fields;
                }
                io:IOError err =>{
                    return err;
                }
            }
        }
        (any|null) =>{
            return empty;
        }

    }
}

function writeRecord (string[] fields) {
    match txtChannel {
        io:DelimitedRecordChannel delimChannel =>{
            var result = delimChannel.writeTextRecord(fields);
        }
        (any|null) =>{
            io:println("Feilds cannot be written");
        }
    }
}

function close () {
    match txtChannel {
        io:DelimitedRecordChannel delimChannel =>{
            var err = delimChannel.closeDelimitedRecordChannel();
        }
        (any|null) =>{
            io:println("Channel cannot be closed");
        }
    }
}


function hasNextRecord () returns (boolean) {
    boolean hasNext;
    match txtChannel {
        io:DelimitedRecordChannel delimChannel =>{
            hasNext = delimChannel.hasNextTextRecord();
            return hasNext;
        }
        (any|null) =>{
            io:println("Channel cannot be closed");
            return hasNext;
        }
    }

}

function loadToTable (string filePath) returns (float|io:IOError) {
    float total;
    var result = io:loadToTable(filePath, "\n", ",", "UTF-8", false, typeof Employee);
    match result {
        table<Employee> tb => {
            foreach x in tb {
                total = total + x.salary;
            }
            return total;
        }
        io:IOError err => {
            return err;
        }
    }
}

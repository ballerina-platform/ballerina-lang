import ballerina/io;

type Employee {
    string id;
    string name;
    float salary;
};

io:DelimitedRecordChannel txtChannel;

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

function initDefaultCsvForReading(string filePath) returns (boolean|io:IOError){
    var csvDefaultChannel = io:createCsvChannel(filePath);
    match csvDefaultChannel {
       io:DelimitedRecordChannel delimChannel =>{
          txtChannel = delimChannel;
          return true;
       }
       io:IOError err =>{
          return err;
       }
    }
}

function initDefaultCsvForWriting(string filePath) returns (boolean|io:IOError){
    var csvDefaultChannel = io:createCsvChannel(filePath,mode="w");
    match csvDefaultChannel {
        io:DelimitedRecordChannel delimChannel =>{
            txtChannel = delimChannel;
            return true;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function initRfcForReading(string filePath) returns (boolean|io:IOError){
    var csvDefaultChannel = io:createCsvChannel(filePath,mode="r", rf="RFC4180");
    match csvDefaultChannel {
       io:DelimitedRecordChannel delimChannel =>{
          txtChannel = delimChannel;
          return true;
       }
       io:IOError err =>{
          return err;
       }
    }
}

function initRfcForWriting(string filePath) returns (boolean|io:IOError){
    var csvDefaultChannel = io:createCsvChannel(filePath,mode="w", rf="RFC4180");
    match csvDefaultChannel {
        io:DelimitedRecordChannel delimChannel =>{
            txtChannel = delimChannel;
            return true;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function initTdfForReading(string filePath) returns (boolean|io:IOError){
    var csvDefaultChannel = io:createCsvChannel(filePath,mode="r", rf="TDF");
    match csvDefaultChannel {
       io:DelimitedRecordChannel delimChannel =>{
         txtChannel = delimChannel;
         return true;
       }
       io:IOError err =>{
         return err;
       }
    }
}

function initTdfForWriting(string filePath) returns (boolean|io:IOError){
    var csvDefaultChannel = io:createCsvChannel(filePath,mode="w", rf="TDF");
    match csvDefaultChannel {
        io:DelimitedRecordChannel delimChannel =>{
            txtChannel = delimChannel;
            return true;
        }
        io:IOError err =>{
            return err;
        }
    }
}

function nextRecord () returns (string[]|io:IOError) {
    var result = txtChannel.nextTextRecord();
    match result {
        string[] fields => {
            return fields;
        }
        io:IOError err => {
            return err;
        }
    }
}

function writeRecord (string[] fields) {
    var result = txtChannel.writeTextRecord(fields);
}

function close () {
    var err = txtChannel.closeDelimitedRecordChannel();
}


function hasNextRecord () returns (boolean) {
    return txtChannel.hasNextTextRecord();

}

function loadToTable (string filePath) returns (float|io:IOError) {
    float total;
    var result = io:loadToTable(filePath, "\n", ",", "UTF-8", false, Employee);
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

import ballerina/io;

io:CSVChannel? csvChannel;

type Employee {
    string id;
    string name;
    float salary;
};

function initCSVChannel(string filePath, io:Mode permission, string encoding, io:Separator fieldSeparator) {
    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    io:CharacterChannel charChannel = new io:CharacterChannel(byteChannel, encoding);
    csvChannel = new io:CSVChannel(charChannel, fs = fieldSeparator);
}

function initOpenCsv(string filePath, io:Mode permission, string encoding, io:Separator fs, int nHeaders = 0) {
    csvChannel = io:openCsvFile(filePath, fieldSeparator=fs, skipHeaders = nHeaders);
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
            error e = {message:"Record channel not initialized properly"};
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

function getTable(string filePath, io:Mode permission, string encoding, io:Separator fieldSeperator) returns float|error
{
    io:ByteChannel byteChannel = io:openFile(filePath, permission);
    io:CharacterChannel charChannel = new io:CharacterChannel(byteChannel, encoding);
    io:CSVChannel csv = new io:CSVChannel(charChannel, fs = fieldSeperator);
    float total;
    match csv.getTable(Employee) {
        table<Employee> tb => {
            foreach x in tb {
                total = total + x.salary;
            }
            return total;
        }
        error err => {
            return err;
        }

    }
}

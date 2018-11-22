import ballerina/io;

type Employee record {
    string id;
    string name;
    float salary;
};

io:ReadableCSVChannel? rch = ();
io:WritableCSVChannel? wch = ();

const string IO_ERROR_CODE = "{ballerina/io}IOError";

function initReadableCsvChannel(string filePath, string encoding, io:Separator fieldSeparator) {
    io:ReadableByteChannel byteChannel = untaint io:openReadableFile(filePath);
    io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
    rch = new io:ReadableCSVChannel(charChannel, fs = fieldSeparator);
}

function initWritableCsvChannel(string filePath, string encoding, io:Separator fieldSeparator) {
    io:WritableByteChannel byteChannel = untaint io:openWritableFile(filePath);
    io:WritableCharacterChannel charChannel = new io:WritableCharacterChannel(byteChannel, encoding);
    wch = new io:WritableCSVChannel(charChannel, fs = fieldSeparator);
}

function initOpenCsvChannel(string filePath, string encoding, io:Separator fieldSeparator, int nHeaders = 0) {
    io:ReadableByteChannel byteChannel = untaint io:openReadableFile(filePath);
    io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
    rch = new io:ReadableCSVChannel(charChannel, fs = fieldSeparator, nHeaders = nHeaders);
}

function nextRecord() returns (string[]|error) {
    var result = rch.getNext();
    if (result is string[]) {
        return result;
    } else if (result is error) {
        return result;
    } else {
        error e = error(IO_ERROR_CODE, { message : "Record channel not initialized properly" });
        return e;
    }
}

function writeRecord(string[] fields) {
    var result = wch.write(fields);
}

function close() {
    _ = rch.close();
    _ = wch.close();
}


function hasNextRecord() returns boolean? {
    return rch.hasNext();
}

function getTable(string filePath, string encoding, io:Separator fieldSeperator) returns float|error {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    io:ReadableCharacterChannel charChannel = new io:ReadableCharacterChannel(byteChannel, encoding);
    io:ReadableCSVChannel csv = new io:ReadableCSVChannel(charChannel, fs = fieldSeperator);
    float total = 0.0;
    var tableResult = csv.getTable(Employee);
    if (tableResult is table<Employee>) {
        foreach x in tableResult {
            total = total + x.salary;
        }
        return total;
    } else if (tableResult is error) {
        return tableResult;
    } else {
        error e = error(IO_ERROR_CODE, { message : "Record channel not initialized properly" });
        return e;
    }
}

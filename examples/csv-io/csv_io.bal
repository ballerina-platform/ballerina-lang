import ballerina/io;
import ballerina/log;

type Employee record {
    string id;
    string name;
    float salary;
};

// This function reads records one by one and prints the records.
function process(io:ReadableCSVChannel csvChannel)
                                returns @tainted error? {
    // Reads all the records from the provided file
    // until there are no more records.
    while (csvChannel.hasNext()) {
        // Reads the records.
        var records = check csvChannel.getNext();
        // Prints the records.
        if (records is string[]) {
            io:println(records);
        }
    }
    return;
}

//Specifies the location of the `.CSV` file.
public function main() returns @tainted error? {
    string srcFileName = "./files/sample.csv";
    // Opens a CSV channel in the `write` mode and writes some data to
    // the `./files/sample.csv` file for later use.
    // The record separator of the `.CSV` file is a
    // new line and the field separator is a comma (,).
    io:WritableCSVChannel wCsvChannel =
                        check io:openWritableCsvFile(srcFileName);
    string[][] data = [["1", "James", "10000"], ["2", "Nathan", "150000"],
    ["3", "Ronald", "120000"], ["4", "Roy", "6000"],
    ["5", "Oliver", "1100000"]];
    writeDataToCSVChannel(wCsvChannel, ...data);
    closeWritableCSVChannel(wCsvChannel);
    // Opens a CSV channel in the `read` mode, which is the default mode.
    io:ReadableCSVChannel rCsvChannel =
                        check io:openReadableCsvFile(srcFileName);
    io:println("Start processing the CSV file from ", srcFileName);
    var processedResult = process(rCsvChannel);
    if (processedResult is error) {
        log:printError("An error occurred while processing the records: ",
                        err = processedResult);
    }
    io:println("Processing completed.");
    // Closes the CSV channel.
    closeReadableCSVChannel(rCsvChannel);
    // Opens a CSV channel in the `read` mode, which is the default mode.
    io:ReadableCSVChannel rCsvChannel2 =
                            check io:openReadableCsvFile(srcFileName);
    // Reads the `.CSV` file as a `table`.
    io:println("Reading  " + srcFileName + " as a table");
    var tblResult = rCsvChannel2.getTable(Employee);
    if (tblResult is table<Employee>) {
        foreach var rec in tblResult {
            io:println(rec);
        }
    } else {
        log:printError("An error occurred while creating table: ",
                        err = tblResult);
    }
    closeReadableCSVChannel(rCsvChannel2);
    // Opens a CSV channel in the "write" mode and writes the `table` to a `.CSV` file.
    string targetFileName = "./files/output.csv";
    io:WritableCSVChannel wCsvChannel2 =
                        check io:openWritableCsvFile(targetFileName);
    io:println("Creating a table and adding data");
    table<Employee> employeeTable = createTableAndAddData();
    io:println("Writing the table to " + targetFileName);
    foreach var entry in employeeTable {
        string[] rec = [entry.id, entry.name, entry.salary.toString()];
        writeDataToCSVChannel(wCsvChannel2, rec);
    }
    closeWritableCSVChannel(wCsvChannel2);
}

// Creates a `table` and adds some data.
function createTableAndAddData() returns table<Employee> {
    table<Employee> employeeTable = table{};
    Employee[] employees = [];
    employees[0] = { id: "1", name: "Allen", salary: 300000.0 };
    employees[1] = { id: "2", name: "Wallace", salary: 200000.0 };
    employees[2] = { id: "3", name: "Sheldon", salary: 1000000.0 };
    foreach var employee in employees {
        var result = employeeTable.add(employee);
        if (result is error) {
            log:printError("Error occurred while adding data to table: ",
                            err = result);
        }
    }
    return employeeTable;
}

// Writes data to a given CSV Channel.
function writeDataToCSVChannel(io:WritableCSVChannel csvChannel,
                                string[]... data) {
    foreach var rec in data {
        var returnedVal = csvChannel.write(rec);
        if (returnedVal is error) {
            log:printError("Error occurred while writing to target file: ",
                            err = returnedVal);
        }
    }
}

// Closes the Readable CSV channel.
function closeReadableCSVChannel(io:ReadableCSVChannel csvChannel) {
    var result = csvChannel.close();
    if (result is error) {
        log:printError("Error occurred while closing the channel: ",
                        err = result);
    }
}

// Closes the Writable CSV channel.
function closeWritableCSVChannel(io:WritableCSVChannel csvChannel) {
    var result = csvChannel.close();
    if (result is error) {
        log:printError("Error occurred while closing the channel: ",
                        err = result);
    }
}

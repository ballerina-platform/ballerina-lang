import ballerina/io;
import ballerina/log;

type Employee record {
    string id;
    string name;
    float salary;
};

// This function reads the next record from the csvChannel.
function readNext(io:ReadableCSVChannel csvChannel) returns string[] {
    match csvChannel.getNext() {
        string[] records => {
            return records;
        }
        error err => {
            throw err;
        }
        () => {
            error e = error("Record channel not initialized properly");
            throw e;
        }
    }
}

// This function reads records one by one and prints the records.
function process(io:ReadableCSVChannel csvChannel) {
    try {
        // Read all the records from the provided file
        // until there are no more records.
        while (csvChannel.hasNext()) {
            // Read the records.
            string[] records = readNext(csvChannel);
            // Print the records.
            io:println(records);
        }
    } catch (error err) {
        throw err;
    }
}
//Specify the location of the `.CSV` file.
public function main() {
    string srcFileName = "./files/sample.csv";

    // Open a CSV channel in `write` mode and write some data to
    // ./files/sample.csv for later use.
    // The record separator of the `.CSV` file is a
    // new line, and the field separator is a comma (,).
    io:WritableCSVChannel wCsvChannel = io:openWritableCsvFile(srcFileName);
    string[][] data = [["1", "James", "10000"], ["2", "Nathan", "150000"],
    ["3", "Ronald", "120000"], ["4", "Roy", "6000"], ["5", "Oliver", "1100000"]];
    writeDataToCSVChannel(wCsvChannel, ...data);
    closeWritableCSVChannel(wCsvChannel);

    // Open a CSV channel in `read` mode which is the default mode.
    io:ReadableCSVChannel rCsvChannel = io:openReadableCsvFile(srcFileName);
    try {
        io:println("Start processing the CSV file from ", srcFileName);
        process(rCsvChannel);
        io:println("Processing completed.");
    } catch (error e) {
        log:printError("An error occurred while processing the records: ",
            err = e);
    } finally {
        // Close the CSV channel.
        closeReadableCSVChannel(rCsvChannel);
    }

    // Open a CSV channel in `read` mode which is the default mode.
    io:ReadableCSVChannel rCsvChannel2 = io:openReadableCsvFile(srcFileName);
    // Read the `.CSV` file as a table.
    io:println("Reading  " + srcFileName + " as a table");
    match rCsvChannel2.getTable(Employee) {
        table<Employee> employeeTable => {
            foreach rec in employeeTable {
                io:println(rec);
            }
        }
        error err => {
            io:println(err.message);
        }
    }
    closeReadableCSVChannel(rCsvChannel2);

    // Writing the a table to a `.CSV` file.
    string targetFileName = "./files/output.csv";
    // Opening CSV channel in "write" mode.
    io:WritableCSVChannel wCsvChannel2 = io:openWritableCsvFile(targetFileName);
    io:println("Creating a table and adding data");
    table<Employee> employeeTable = createTableAndAddData();
    io:println("Writing the table to " + targetFileName);
    foreach entry in employeeTable {
        string[] rec = [entry.id, entry.name, <string>entry.salary];
        writeDataToCSVChannel(wCsvChannel2, rec);
    }
    closeWritableCSVChannel(wCsvChannel2);
}

// Creates a table and adds some data.
function createTableAndAddData() returns table<Employee> {
    table<Employee> employeeTable = table{};

    Employee[] employees;
    employees[0] = { id: "1", name: "Allen", salary: 300000.0 };
    employees[1] = { id: "2", name: "Wallace", salary: 200000.0 };
    employees[2] = { id: "3", name: "Sheldon", salary: 1000000.0 };

    foreach employee in employees {
        match employeeTable.add(employee) {
            error e => io:println("Error occurred while adding data to table ", e);
            () => io:println("Successfully added entry to table");
        }
    }
    return employeeTable;
}

// Write data to a given CSV Channel.
function writeDataToCSVChannel(io:WritableCSVChannel csvChannel, string[]... data) {
    foreach rec in data {
        var returnedVal = csvChannel.write(rec);
        match returnedVal {
            error e => io:println(e.message);
            () => io:println("Record was successfully written to target file");
        }
    }
}

// Close Readable CSV channel.
function closeReadableCSVChannel(io:ReadableCSVChannel csvChannel) {
    match csvChannel.close() {
        error channelCloseError => {
            log:printError("Error occured while closing the channel: ",
                err = channelCloseError);
        }
        () => io:println("CSV channel closed successfully.");
    }
}

// Close Writable CSV channel.
function closeWritableCSVChannel(io:WritableCSVChannel csvChannel) {
    match csvChannel.close() {
        error channelCloseError => {
            log:printError("Error occured while closing the channel: ",
                err = channelCloseError);
        }
        () => io:println("CSV channel closed successfully.");
    }
}

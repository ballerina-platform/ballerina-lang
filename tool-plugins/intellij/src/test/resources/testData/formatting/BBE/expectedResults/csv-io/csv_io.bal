import ballerina/io;
import ballerina/log;

type Employee record {
    string id,
    string name,
    float salary,
};

// This function reads the next record from the channel.
function readNext(io:CSVChannel channel) returns string[] {
    match channel.getNext() {
        string[] records => {
            return records;
        }
        error err => {
            throw err;
        }
        () => {
            error e = { message: "Record channel not initialized properly" };
            throw e;
        }
    }
}

// This function reads records one by one and prints the records.
function process(io:CSVChannel channel) {
    try {
        // Read all the records from the provided file
        // until there are no more records.
        while (channel.hasNext()) {
            // Read the records.
            string[] records = readNext(channel);
            // Print the records.
            io:println(records);
        }
    } catch (error err) {
        throw err;
    }
}
//Specify the location of the `.CSV` file.
function main(string... args) {
    string srcFileName = "./files/sample.csv";

    // Open a CSV channel in `write` mode and write some data to
    // ./files/sample.csv for later use.
    // The record separator of the `.CSV` file is a
    // new line, and the field separator is a comma (,).
    io:CSVChannel csvChannel = io:openCsvFile(srcFileName, mode = "w");
    string[][] data = [["1", "James", "10000"], ["2", "Nathan", "150000"],
    ["3", "Ronald", "120000"], ["4", "Roy", "6000"], ["5", "Oliver", "1100000"]];
    writeDataToCSVChannel(csvChannel, ...data);
    closeCSVChannel(csvChannel);

    // Open a CSV channel in `read` mode which is the default mode.
    csvChannel = io:openCsvFile(srcFileName);
    try {
        io:println("Start processing the CSV file from ", srcFileName);
        process(csvChannel);
        io:println("Processing completed.");
    } catch (error e) {
        log:printError("An error occurred while processing the records: ",
            err = e);
    } finally {
        // Close the CSV channel.
        closeCSVChannel(csvChannel);
    }

    // Open a CSV channel in `read` mode which is the default mode.
    csvChannel = io:openCsvFile(srcFileName);
    // Read the `.CSV` file as a table.
    io:println("Reading  " + srcFileName + " as a table");
    match csvChannel.getTable(Employee) {
        table<Employee> employeeTable => {
            foreach rec in employeeTable {
                io:println(rec);
            }
        }
        error err => {
            io:println(err.message);
        }
    }
    closeCSVChannel(csvChannel);

    // Writing the a table to a `.CSV` file.
    string targetFileName = "./files/output.csv";
    // Opening CSV channel in "write" mode.
    csvChannel = io:openCsvFile(targetFileName, mode = "w");
    io:println("Creating a table and adding data");
    table<Employee> employeeTable = createTableAndAddData();
    io:println("Writing the table to " + targetFileName);
    foreach entry in employeeTable {
        string[] rec = [entry.id, entry.name, <string>entry.salary];
        writeDataToCSVChannel(csvChannel, rec);
    }
    closeCSVChannel(csvChannel);
}

// Creates a table and adds some data.
function createTableAndAddData() returns table<Employee> {
    table<Employee> employeeTable = table{};

    Employee[] employees;
    employees[0] = { id: "1", name: "Allen", salary: 300000 };
    employees[1] = { id: "2", name: "Wallace", salary: 200000 };
    employees[2] = { id: "3", name: "Sheldon", salary: 1000000 };

    foreach employee in employees {
        match employeeTable.add(employee) {
            error e => io:println("Error occurred while adding data to table ", e);
            () => io:println("Successfully added entry to table");
        }
    }
    return employeeTable;
}

// Write data to a given CSV Channel.
function writeDataToCSVChannel(io:CSVChannel csvChannel, string[]... data) {
    foreach rec in data {
        var returnedVal = csvChannel.write(rec);
        match returnedVal {
            error e => io:println(e.message);
            () => io:println("Record was successfully written to target file");
        }
    }
}

// Close the CSV channel.
function closeCSVChannel(io:CSVChannel csvChannel) {
    match csvChannel.close() {
        error channelCloseError => {
            log:printError("Error occured while closing the channel: ",
                err = channelCloseError);
        }
        () => io:println("CSV channel closed successfully.");
    }
}

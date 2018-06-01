import ballerina/io;
import ballerina/log;

type Employee {
    string id,
    string name,
    float salary,
};

type Salary {
    string id,
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
            error e = { message:"Record channel not initialized properly" };
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
    // The record separator of the `.CSV` file is a
    // new line, and the field separator is a comma (,).
    io:CSVChannel csvChannel = io:openCsvFile(srcFileName);
    try {
        io:println("Start processing the CSV file from ", srcFileName);
        process(csvChannel);
        io:println("Processing completed.");
    } catch (error e) {
        log:printError("An error occurred while processing the records: ",
                        err = e);
    } finally {
        // Close the text record channel.
        closeCSVChannel(csvChannel);
    }

    // Read the `.CSV` file as a table.
    csvChannel = io:openCsvFile(srcFileName);
    table<Salary> salaryTable;
    try {
        io:println("Reading  " + srcFileName + " as a table");
        match csvChannel.getTable(Employee) {
            table<Employee> employeeTable => {
                io:println("Creating `salaryTable` using `select` operation");
                salaryTable = employeeTable.select(getSalary);
            }
            error err => {
                io:println(err.message);
            }
        }
    } catch (error e) {
        log:printError("An error occurred while reading from CSV channel: ", err = e);
    } finally {
        closeCSVChannel(csvChannel);
    }

    // Writing the newly created table to a `.CSV` file.
    string targetFileName = "./files/output.csv";
    // Opening CSV channel in "write" mode.
    csvChannel = io:openCsvFile(targetFileName, mode = "w");
    try {
        io:println("Writing the table to " + targetFileName);
        foreach (entry in salaryTable) {
            string[] record = [entry.id, <string>entry.salary];
            var returnedVal = csvChannel.write(record);
            match returnedVal {
                error e => io:println(e.message);
                () => io:println("Record [\"" + entry.id + "\", \"" + entry.salary +
                        "\"] was successfully written to " + targetFileName);
            }
        }
    } catch (error e) {
        log:printError("An error occurred while writing to CSV channel: ", err = e);
    } finally {
        closeCSVChannel(csvChannel);
    }
}

// Close the text record channel.
function closeCSVChannel(io:CSVChannel csvChannel) {
    match csvChannel.close() {
        error channelCloseError => {
            log:printError("Error occured while closing the channel: ",
                err = channelCloseError);
        }
        () => io:println("CSV channel closed successfully.");
    }
}

// Lamba function used to perform `select` operation on a `table`.
function getSalary(Employee e) returns Salary {
    Salary s = { id: e.id, salary: e.salary };
    return s;
}

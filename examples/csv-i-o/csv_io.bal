import ballerina/io;

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
            error e = {message: "Record channel not initialized properly"};
            throw e;
        }
    }
}

// This function read records one by one and print.
function process(io:CSVChannel channel) {
    try {
        // Read all the records from the provided file until there are no more records.
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
    //io:CSVChannel csvChannel = io:openCsvFile(srcFileName, mode="r",fieldSeparator=",",charset="UTF-8",skipHeaders=0);
    io:CSVChannel csvChannel = io:openCsvFile(srcFileName);
    try {
        io:println("Start processing the CSV file from ", srcFileName);
        process(csvChannel);
        io:println("Processing completed.");
    } catch (error err) {
        io:println("An error occurred while processing the records. ", err.message);
    } finally {
        //Close the text record channel.
        match csvChannel.close() {
            error sourceCloseError => {
                io:println("Error occured while closing the channel: ", sourceCloseError.message);
            }
            () => {
                io:println("Source channel closed successfully.");
            }
        }
    }
}

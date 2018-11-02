import ballerina/io;
import ballerina/log;

// This function returns a `ReadableTextRecordChannel` from a given file location.
// The encoding is a character representation (i.e., UTF-8 ASCCI) of the
// content in the file. The `rs` annotation defines a record seperator
// (e.g., a new line) and the `fs` annotation is a field seperator
// (e.g., a comma).
function getReadableRecordChannel(string filePath, string encoding, string rs,
                              string fs) returns (io:ReadableTextRecordChannel) {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    // Create a `readable character channel`
    // from the `readable byte channel` to read content as text.
    io:ReadableCharacterChannel characterChannel = new(byteChannel, encoding);
    // Convert the `readable character channel` to a `readable record channel`
    //to read the content as records.
    io:ReadableTextRecordChannel delimitedRecordChannel = new(characterChannel,
                                                               rs = rs,
                                                               fs = fs);
    return delimitedRecordChannel;
}

// This function returns a `WritableTextRecordChannel` from a given file location.
// The encoding is a character representation (i.e., UTF-8 ASCCI) of the
// content in the file. The `rs` annotation defines a record seperator
// (e.g., a new line) and the `fs` annotation is a field seperator
// (e.g., a comma).
function getWritableRecordChannel(string filePath, string encoding, string rs,
                                      string fs) returns (io:WritableTextRecordChannel) {
    io:WritableByteChannel byteChannel = io:openWritableFile(filePath);
    // Create a `writable character channel`
    // from the `writable byte channel` to read content as text.
    io:WritableCharacterChannel characterChannel = new(byteChannel, encoding);
    // Convert the `writable character channel` to a `writable record channel`
    //to read the content as records.
    io:WritableTextRecordChannel delimitedRecordChannel = new(characterChannel,
        rs = rs,
        fs = fs);
    return delimitedRecordChannel;
}

// This function processes the `.CSV` file and
// writes content back as text with the `|` delimiter.
function process(io:ReadableTextRecordChannel srcRecordChannel,
                 io:WritableTextRecordChannel dstRecordChannel) {
    // Read all the records from the provided file until there are
    // no more records.
    while (srcRecordChannel.hasNext()) {
        // Read the records.
        string[] records = check srcRecordChannel.getNext();
        // Write the records.
        var result =check dstRecordChannel.write(records);
    }
}

//Specify the location of the `.CSV` file and the text file. 
public function main() {
    string srcFileName = "./files/sample.csv";
    string dstFileName = "./files/sampleResponse.txt";
    // The record separator of the `.CSV` file is a
    // new line, and the field separator is a comma (,).
    io:ReadableTextRecordChannel srcRecordChannel =
        getReadableRecordChannel(srcFileName, "UTF-8", "\\r?\\n", ",");
    //The record separator of the text file
    //is a new line, and the field separator is a pipe (|).
    io:WritableTextRecordChannel dstRecordChannel =
        getWritableRecordChannel(dstFileName, "UTF-8", "\r\n", "|");
    try {
        io:println("Start processing the CSV file from " + srcFileName +
                   " to the text file in " + dstFileName);
        process(srcRecordChannel, dstRecordChannel);
        io:println("Processing completed. The processed file is located in ",
                   dstFileName);
    } catch (error e) {
        log:printError("An error occurred while processing the records: ",
                        err = e);
    } finally {
        //Close the text record channel.
        match srcRecordChannel.close() {
            error sourceCloseError => {
                log:printError("Error occured while closing the channel: ",
                                err = sourceCloseError);
            }
            () => {
                io:println("Source channel closed successfully.");
            }
        }
        match dstRecordChannel.close() {
            error destinationCloseError => {
                log:printError("Error occured while closing the channel: ",
                                err = destinationCloseError);
            }
            () => {
                io:println("Destination channel closed successfully.");
            }
        }
    }
}

import ballerina/io;
import ballerina/log;

// This function returns a `ReadableTextRecordChannel` from a given file location.
// The encoding is a character representation (i.e., UTF-8 ASCCI) of the
// content in the file. The `rs` parameter defines a record separator
// (e.g., a new line) and the `fs` parameter is a field separator
// (e.g., a comma).
function getReadableRecordChannel(string filePath, string encoding,
                        string rs, string fs)
                        returns @tainted io:ReadableTextRecordChannel|error {
    io:ReadableByteChannel byteChannel = check io:openReadableFile(filePath);
    // Creates a readable character channel
    // from the readable byte channel to read the content as text.
    io:ReadableCharacterChannel characterChannel = new(byteChannel, encoding);
    // Converts the readable character channel to a readable record channel
    // to read the content as records.
    io:ReadableTextRecordChannel delimitedRecordChannel = new(characterChannel,
                                                              rs = rs,
                                                              fs = fs);
    return delimitedRecordChannel;
}

// This function returns a `WritableTextRecordChannel` from a given file location.
// The encoding is a character representation (i.e., UTF-8 ASCCI) of the
// content in the file. The `rs` parameter defines a record separator
// (e.g., a new line) and the `fs` parameter is a field separator
// (e.g., a comma).
function getWritableRecordChannel(string filePath, string encoding, string rs,
                    string fs)
                    returns @tainted io:WritableTextRecordChannel|error {
    io:WritableByteChannel byteChannel = check io:openWritableFile(filePath);
    // Creates a writable character channel
    // from the writable byte channel to read the content as text.
    io:WritableCharacterChannel characterChannel = new(byteChannel, encoding);
    // Converts the writable character channel to a writable record channel
    // to read the content as records.
    io:WritableTextRecordChannel delimitedRecordChannel = new(characterChannel,
                                                              rs = rs,
                                                              fs = fs);
    return delimitedRecordChannel;
}

// This function processes the `.CSV` file and
// writes the content back as text with the `|` delimiter.
function process(io:ReadableTextRecordChannel srcRecordChannel,
                 io:WritableTextRecordChannel dstRecordChannel)
                 returns @tainted error? {
    // Reads all the records from the provided file until there are
    // no more records.
    while (srcRecordChannel.hasNext()) {
        // Reads the records.
        string[] records = check srcRecordChannel.getNext();
        // Writes the records.
        var result = check dstRecordChannel.write(records);
    }
    return;
}

// Closes the readable text record channel.
function closeRc(io:ReadableTextRecordChannel rc) {
    var closeResult = rc.close();
    if (closeResult is error) {
        log:printError("Error occurred while closing the channel: ", closeResult);
    }
}

// Closes the writable channel.
function closeWc(io:WritableTextRecordChannel wc) {
    var closeResult = wc.close();
    if (closeResult is error) {
        log:printError("Error occurred while closing the channel: ", closeResult);
    }
}

//Specifies the location of the `.CSV` file and the text file. 
public function main() {
    string srcFileName = "./files/sample.csv";
    string dstFileName = "./files/sampleResponse.txt";

    // The record separator of the `.CSV` file is a
    // new line and the field separator is a comma (,).
    io:ReadableTextRecordChannel srcRecordChannel;
    var readableChannel = getReadableRecordChannel(srcFileName,
                                                        "UTF-8", "\\r?\\n", ",");
    if (readableChannel is error) {
        log:printError("An error occurred while creating readable record channel. ",
                        readableChannel);
        return;
    } else {
        srcRecordChannel = readableChannel;
    }

    //The record separator of the text file
    //is a new line and the field separator is a pipe (|).
    io:WritableTextRecordChannel dstRecordChannel;
    var writableChannel = getWritableRecordChannel(dstFileName,
                                                        "UTF-8", "\r\n", "|");
    if (writableChannel is error) {
        log:printError("An error occurred while creating writable record channel. ",
                        writableChannel);
        return;
    } else {
        dstRecordChannel = writableChannel;
    }

    io:println("Start processing the CSV file from " + srcFileName +
               " to the text file in " + dstFileName);
    var result = process(srcRecordChannel, dstRecordChannel);
    if (result is error) {
        log:printError("An error occurred while processing the records: ", result);
    } else {
        io:println("Processing completed. The processed file is located in ",
                    dstFileName);
    }
    // Closes the channels.
    closeRc(srcRecordChannel);
    closeWc(dstRecordChannel);
}

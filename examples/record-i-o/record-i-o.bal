import ballerina/io;

@Description{value:"This function returns a `DelimitedRecordChannel` from a given file location.The encoding is a character representation (i.e., UTF-8 ASCCI) of the content in the file. The `rs` annotation defines a record seperator (e.g., a new line) and the `fs` annotation is a field seperator (e.g., a comma)."}
function getFileRecordChannel (string filePath, string permission, string encoding,
                               string rs, string fs) returns (io:DelimitedRecordChannel) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    // Create a `character channel` from the `byte channel` to read content as text.
    var channelResp = io:createCharacterChannel(channel, encoding);
    match channelResp {
        io:CharacterChannel characterChannel => {
            // Convert the `character channel` to a `record channel`
            //to read the content as records.
            var recordChannelResp = io:createDelimitedRecordChannel(characterChannel, rs, fs);
            match recordChannelResp {
                io:DelimitedRecordChannel delimitedRecordChannel => {
                    return delimitedRecordChannel;
                }
                io:IOError err => {
                    throw err.cause but {() => err};
                }
            }
        }
        io:IOError err => {
            throw err.cause but {() => err};
        }
    }
}

@Description{value:"This function reads the next record from the channel."}
function readNext(io:DelimitedRecordChannel channel)returns (string []){
    var recordResp = channel.nextTextRecord();
    match recordResp {
        string[] records => {
            return records;
        }
        io:IOError err => {
            throw err.cause but {() => err};
        }

    }
}

@Description{value:"This function writes the next record to the channel."}
function write(io:DelimitedRecordChannel channel,string [] records){
    io:IOError err = channel.writeTextRecord(records);
    if(err != null){
       throw err.cause but {() => err};
    }
}

@Description{value:"This function processes the `.CSV` file and writes content back as text with the `|` delimiter."}
function process (io:DelimitedRecordChannel srcRecordChannel,
                  io:DelimitedRecordChannel dstRecordChannel) {
    try {
        //Read all the records from the provided file until there are no more records.
        while (srcRecordChannel.hasNextTextRecord()) {
            string[] records;
            // Read the records.
            records = readNext(srcRecordChannel);
            // Write the records.
            write(dstRecordChannel, records);
        }
    }catch (error err) {
        throw err;
    }
}
//Specify the location of the `.CSV` file and the text file. 
function main (string... args) {
    string srcFileName = "./files/sample.csv";
    string dstFileName = "./files/sampleResponse.txt";
    // The record separator of the `.CSV` file is a
    // new line, and the field separator is a comma (,).
    io:DelimitedRecordChannel srcRecordChannel =
    getFileRecordChannel(srcFileName, "r", "UTF-8", "\\r?\\n", ",");
    //The record separator of the text file
    //is a new line, and the field separator is a pipe (|).
    io:DelimitedRecordChannel dstRecordChannel =
    getFileRecordChannel(dstFileName, "w", "UTF-8", "\n", "|");
    try {
        io:println("Start processing the CSV file from " + srcFileName + " to the text file in "
                   + dstFileName);
        process(srcRecordChannel, dstRecordChannel);
        io:println("Processing completed. The processed file is located in "
                   + dstFileName);
    }catch(error err){
        io:println("An error occurred while processing the records. " + err.message);
    }finally {
       //Close the text record channel.
       io:IOError? closeError = srcRecordChannel.closeDelimitedRecordChannel();
       closeError = dstRecordChannel.closeDelimitedRecordChannel();
    }
}

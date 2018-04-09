import ballerina/io;

@Description{value:"This function will return a DelimitedRecordChannel from a given file location.The encoding is character represenation of the content in file i.e UTF-8 ASCCI. The rs is record seperator i.e newline etc. and fs is field seperator i.e comma etc."}
function getFileRecordChannel (string filePath, string permission, string encoding,
                               string rs, string fs) returns (io:DelimitedRecordChannel) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    //Then we create a character channel from the byte channel to read content as text.
    var channelResp = io:createCharacterChannel(channel, encoding);
    match channelResp {
        io:CharacterChannel characterChannel => {
            //Finally we convert the character channel to record channel
            //to read content as records.
            var recordChannelResp = io:createDelimitedRecordChannel(characterChannel, rs, fs);
            match recordChannelResp {
                io:DelimitedRecordChannel delimitedRecordChannel => {
                    return delimitedRecordChannel;
                }
                io:IOError err => {
                    throw err.cause[0];
                }
            }
        }
        io:IOError err => {
            throw err.cause[0];
        }
    }
}

@Description{value:"This function will read next record from the channel"}
function readNext(io:DelimitedRecordChannel channel)returns (string []){
    var recordResp = channel.nextTextRecord();
    match recordResp {
        string[] records => {
            return records;
        }
        io:IOError err => {
            throw err.cause[0];
        }

    }
}

@Description{value:"This function will write next record to the channel"}
function write(io:DelimitedRecordChannel channel,string [] records){
    io:IOError err = channel.writeTextRecord(records);
    if(err != null){
       throw err.cause[0];
    }
}

@Description{value:"This function will process CSV file and write content back as text with '|' delimiter."}
function process (io:DelimitedRecordChannel srcRecordChannel,
                  io:DelimitedRecordChannel dstRecordChannel) {
    try {
        //We read all the records from the provided file until there're no records returned.
        while (srcRecordChannel.hasNextTextRecord()) {
            string[] records;
            //Here's how we read records.
            records = readNext(srcRecordChannel);
            //Here's how we write records.
            write(dstRecordChannel, records);
        }
    }catch (error err) {
        throw err;
    }
}

function main (string[] args) {
    string srcFileName = "./files/sample.csv";
    string dstFileName = "./files/sampleResponse.txt";
    //Here we specify the location of the CSV file where the record separator is
    //new line and field separator is comma.
    io:DelimitedRecordChannel srcRecordChannel =
    getFileRecordChannel(srcFileName, "r", "UTF-8", "\\r?\\n", ",");
    //Here we specify the location of the text file where the record separator
    //is new line and field separator is pipe.
    io:DelimitedRecordChannel dstRecordChannel =
    getFileRecordChannel(dstFileName, "w", "UTF-8", "\n", "|");
    try {
        io:println("Start to process CSV file from " + srcFileName + " to text file in "
                   + dstFileName);
        process(srcRecordChannel, dstRecordChannel);
        io:println("Processing completed. The processed file could be located in "
                   + dstFileName);
    }catch(error err){
        io:println("error occurred while processing records " + err.message);
    }finally {
       //Close the text record channel.
       io:IOError? closeError = srcRecordChannel.closeDelimitedRecordChannel();
       closeError = dstRecordChannel.closeDelimitedRecordChannel();
    }
}

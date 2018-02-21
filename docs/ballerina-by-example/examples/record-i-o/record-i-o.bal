import ballerina.io;

@Description{value:"This function will return a DelimitedRecordChannel from a given file location.The encoding is character represenation of the content in file i.e UTF-8 ASCCI. The rs is record seperator i.e newline etc. and fs is field seperator i.e comma etc."}
function getFileRecordChannel (string filePath, string permission, string encoding,
                               string rs, string fs) (io:DelimitedRecordChannel) {
    //First we get the ByteChannel representation of the file.
    io:ByteChannel channel = io:openFile(filePath, permission);
    //Then we create a character channel from the byte channel to read content as text.
    io:CharacterChannel characterChannel = io:createCharacterChannel(channel, encoding);
    //Finally we convert the character channel to record channel
    //to read content as records.
    io:DelimitedRecordChannel delimitedRecordChannel = io:createDelimitedRecordChannel(characterChannel, rs, fs);
    return delimitedRecordChannel;
}

@Description{value:"This function will process CSV file and write content back as text with '|' delimiter."}
function process (io:DelimitedRecordChannel srcRecordChannel,
                  io:DelimitedRecordChannel dstRecordChannel) {
    //We read all the records from the provided file until there're no records returned.
    while (srcRecordChannel.hasNextTextRecord()) {
        //Here's how we read records.
        string[] records = srcRecordChannel.nextTextRecord();
        //Here's how we write records.
        dstRecordChannel.writeTextRecord(records);
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
    io:println("Start to process CSV file from " + srcFileName + " to text file in "
            + dstFileName);
    process(srcRecordChannel, dstRecordChannel);
    io:println("Processing completed. The processed file could be located in "
            + dstFileName);
    //Close the text record channel.
    srcRecordChannel.closeTextRecordChannel();
    dstRecordChannel.closeTextRecordChannel();
}
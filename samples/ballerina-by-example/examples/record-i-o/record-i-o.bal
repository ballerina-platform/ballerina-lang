import ballerina.file;
import ballerina.io;

@Description{value:"This function will return a TextRecordChannel from a given file location.The encoding is character represenation of the content in file i.e UTF-8 ASCCI. The rs is record seperator i.e newline etc. and fs is field seperator i.e comma etc."}
function getFileRecordChannel (string filePath, string permission, string encoding,
                               string rs, string fs) (io:TextRecordChannel) {
    file:File src = {path:filePath};
    //First we get the ByteChannel representation of the file
    io:ByteChannel channel = src.openChannel(permission);
    //Then we convert the byte channel to character channel to read content as text
    io:CharacterChannel characterChannel = channel.toCharacterChannel(encoding);
    //Finally we convert the character channel to record channel
    //to read content as records
    io:TextRecordChannel textRecordChannel = characterChannel.
                                             toTextRecordChannel(rs, fs);
    return textRecordChannel;
}

@Description{value:"This function will process CSV file and write content back as text with '|' delimiter."}
function process (io:TextRecordChannel srcRecordChannel,
                  io:TextRecordChannel dstRecordChannel) {
    int numberOfFields = -1;
    //We read all the records from the provided file until there're no records returned
    while (numberOfFields != 0) {
        //Here's how we read records
        string[] records = srcRecordChannel.readTextRecord();
        //Here's how we write records
        dstRecordChannel.writeTextRecord(records);
        numberOfFields = lengthof records;
    }
}

function main (string[] args) {
    string srcFileName = "./files/sample.csv";
    string dstFileName = "./files/sampleResponse.txt";
    //Here we specify the location of the CSV file where the record separator is
    //new line and field separator is comma
    io:TextRecordChannel srcRecordChannel =
    getFileRecordChannel(srcFileName, "r", "UTF-8", "\\r?\\n", ",");
    //Here we specify the location of the text file where the record separator
    //is new line and field separator is pipe
    io:TextRecordChannel dstRecordChannel =
    getFileRecordChannel(dstFileName, "w", "UTF-8", "\n", "|");
    println("Start to process CSV file from " + srcFileName + " to text file in "
            + dstFileName);
    process(srcRecordChannel, dstRecordChannel);
    println("Processing completed. The processed file could be located in "
            + dstFileName);
}

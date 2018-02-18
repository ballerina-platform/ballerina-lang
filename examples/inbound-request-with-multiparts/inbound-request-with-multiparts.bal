import ballerina.net.http;
import ballerina.mime;
import ballerina.io;
import ballerina.file;

@http:configuration {basePath:"/foo"}
service<http> echo {
    @http:resourceConfig {
        methods:["POST"],
        path:"/receivableParts"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        //Extract multiparts from the inbound request
        mime:Entity[] bodyParts = req.getMultiparts();
        int i = 0;

        println("CONTENT TYPE OF TOP LEVEL ENTITY > " + req.getHeader("Content-Type"));
        //Loop through body parts
        while (i < lengthof bodyParts) {
            mime:Entity part = bodyParts[i];
            println("============================PART "+ i +"================================");
            println("---------Content Type-------");
            println(part.contentType.toString());
            println("------------Part Name-------");
            println(part.contentDisposition.name);
            println("------Body Part Content-----");
            handleContent(part);
            i = i + 1;
        }
        http:OutResponse res = {};
        res.setStringPayload("Multiparts Received!");
        _ = conn.respond(res);
    }
}

function handleContent (mime:Entity bodyPart) {
    string contentType = bodyPart.contentType.toString();
    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
        //Extract xml data from body part and print
        println(bodyPart.getXml());
    } else if (mime:APPLICATION_JSON == contentType) {
        //Extract json data from body part and print
        println(bodyPart.getJson());
    } else if (mime:TEXT_PLAIN == contentType){
        //Extract text data from body part and print
        println(bodyPart.getText());
    } else if ("application/vnd.ms-powerpoint" == contentType) {
        //Get a byte channel from body part and write content to a file
        writeToFile(bodyPart.getByteChannel());
        println("Content saved to file");
    }
}

function writeToFile(io:ByteChannel byteChannel) {
    string dstFilePath = "./files/savedFile.ppt";
    io:ByteChannel destinationChannel = getByteChannel(dstFilePath, "w");
    blob readContent;
    int numberOfBytesRead;
    readContent,numberOfBytesRead = byteChannel.readAllBytes();
    int numberOfBytesWritten = destinationChannel.writeBytes(readContent, 0);
}

function getByteChannel (string filePath, string permission) (io:ByteChannel) {
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
    return channel;
}

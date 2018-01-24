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
        //Loop through body parts
        while (i < lengthof bodyParts) {
            mime:Entity part = bodyParts[i];
            println("-----------------------------");
            print("Content Type : ");
            println(part.contentType.toString());
            println("-----------------------------");
            handleContent(part);
            i = i + 1;
        }
        http:OutResponse res = {};
        res.setStringPayload("Multiparts Received!");
        _ = conn.respond(res);
    }
}

@Description {value:"User should write his/her own logic to handle body parts according to his/her requirement."}
function handleContent (mime:Entity bodyPart) {
    string contentType = bodyPart.contentType.toString();
    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
        //Given a body part get it's xml content and print
        println(mime:getXml(bodyPart));
    } else if (mime:APPLICATION_JSON == contentType) {
        //Given a body part get it's json content and print
        println(mime:getJson(bodyPart));
    } else if (mime:TEXT_PLAIN == contentType){
        //Given a body part get it's text content and print
        println(mime:getText(bodyPart));
    } else if ("application/vnd.ms-powerpoint" == contentType) {
        //Given a body part get it's content as a blob and write it to a file
        writeToFile(mime:getBlob(bodyPart));
        println("Content saved to file");
    }
}

@Description {value:"Write a given blob content to a file."}
function writeToFile(blob  readContent) {
    string dstFilePath = "./files/savedFile.ppt";
    io:ByteChannel destinationChannel = getByteChannel(dstFilePath, "w");
    int numberOfBytesWritten = destinationChannel.writeBytes(readContent, 0);
}

@Description {value:"Get a byte channel for the given file."}
function getByteChannel (string filePath, string permission) (io:ByteChannel) {
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
    return channel;
}

import ballerina.io;
import ballerina.mime;
import ballerina.net.http;

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
            io:println("-----------------------------");
            io:print("Content Type : ");
            io:println(part.contentType.toString());
            io:println("-----------------------------");
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
        //Given a body part get it's xml content and io:print
        io:println(mime:getXml(bodyPart));
    } else if (mime:APPLICATION_JSON == contentType) {
        //Given a body part get it's json content and io:print
        io:println(mime:getJson(bodyPart));
    } else if (mime:TEXT_PLAIN == contentType){
        //Given a body part get it's text content and io:print
        io:println(mime:getText(bodyPart));
    } else if ("application/vnd.ms-powerpoint" == contentType) {
        //Given a body part get it's content as a blob and write it to a file
        writeToFile(mime:getBlob(bodyPart));
        io:println("Content saved to file");
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
    io:ByteChannel channel = io:openFile(filePath, permission);
    return channel;
}

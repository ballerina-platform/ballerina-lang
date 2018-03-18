import ballerina.net.http;
import ballerina.mime;
import ballerina.io;

service<http> multiparts {
    @http:resourceConfig {
        methods:["POST"],
        path:"/receivableParts"
    }
    resource receiveMultiparts (http:Connection conn, http:Request req) {
        //Extract multiparts from the inbound request.
        var bodyParts, payloadError = req.getMultiparts();
        http:Response res = {};
        if (payloadError == null) {
            int i = 0;
            io:println("Content-Type of top level entity > " + req.getHeader("content-type"));
            //Loop through body parts
            while (i < lengthof bodyParts) {
                mime:Entity part = bodyParts[i];
                io:println("Part " + i);
                io:println("---------Content Type-------");
                io:println(part.contentType.toString());
                io:println("----------Part Name---------");
                io:println(part.contentDisposition.name);
                io:println("------Body Part Content-----");
                handleContent(part);
                i = i + 1;
            }
            res.setStringPayload("Multiparts Received!");
        } else {
            res = {statusCode:500};
            res.setStringPayload(payloadError.message);
        }

        _ = conn.respond(res);
    }
}

//Handling body part content logic varies according to user's requirement.
function handleContent (mime:Entity bodyPart) {
    string contentType = bodyPart.contentType.toString();
    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
        //Extract xml data from body part and print.
        var xmlContent, _ = bodyPart.getXml();
        io:println(xmlContent);
    } else if (mime:APPLICATION_JSON == contentType) {
        //Extract json data from body part and print.
        var jsonContent, _ = bodyPart.getJson();
        io:println(jsonContent);
    } else if (mime:TEXT_PLAIN == contentType) {
        //Extract text data from body part and print.
        var textContent, _ = bodyPart.getText();
        io:println(textContent);
    } else if ("application/vnd.ms-powerpoint" == contentType) {
        //Get a byte channel from body part and write content to a file.
        var byteChannel, _ = bodyPart.getByteChannel();
        writeToFile(byteChannel);
        byteChannel.close();
        io:println("Content saved to file");
    }
}

function writeToFile (io:ByteChannel byteChannel) {
    string dstFilePath = "./files/savedFile.ppt";
    io:ByteChannel destinationChannel = getByteChannel(dstFilePath, "w");
    blob readContent;
    int numberOfBytesRead = 1;
    while (numberOfBytesRead != 0) {
        readContent, numberOfBytesRead = byteChannel.readBytes(10000);
        int numberOfBytesWritten = destinationChannel.writeBytes(readContent, 0);
    }
}

function getByteChannel (string filePath, string permission) (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    return channel;
}

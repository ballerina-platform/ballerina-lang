import ballerina/net.http;
import ballerina/mime;
import ballerina/io;

endpoint<http:Service> multipartEP {
    port:9090
}

@http:serviceConfig { endpoints:[multipartEP] }
service<http:Service> multiparts {
      @http:resourceConfig {
        methods:["POST"],
        path:"/decode_in_request"
    }
    resource receiveMultiparts (http:ServerConnector conn, http:Request request) {
        //Extract multiparts from the inbound request.
        var bodyParts, payloadError = request.getMultiparts();
        http:Response response = {};
        if (payloadError == null) {
            int i = 0;
            io:println("Content-Type of top level entity > " + request.getHeader("content-type"));
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
            response.setStringPayload("Multiparts Received!");
        } else {
            response = {statusCode:500};
            response.setStringPayload(payloadError.message);
        }

        _ = conn -> respond(response);
    }
}

@Description {value:"Handling body part content logic varies according to user's requirement.."}
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
        _ = byteChannel.close();
        io:println("Content saved to file");
    }
}

//Code snippets following this point are not directly related to multiparts. It makes use of io package for
//byte read and write operations from byte channel.
function getByteChannel (string filePath, string permission) (io:ByteChannel) {
    io:ByteChannel channel = io:openFile(filePath, permission);
    return channel;
}

@Description {value:"Write byte channel content to a file."}
function writeToFile (io:ByteChannel byteChannel) {
    string dstFilePath = "./files/savedFile.ppt";
    io:ByteChannel destinationChannel = getByteChannel(dstFilePath, "w");

    // Specifies the number of bytes that should be read from a single read operation.
    int bytesChunk = 10000;
    int numberOfBytesWritten = 0;
    int readCount = 0;
    int offset = 0;
    blob readContent;
    boolean done = false;
    try {
        // Here is how to specify to read all the content from
        // the source and copy it to the destination.
        while (!done) {
            readContent, readCount = readBytes(byteChannel, bytesChunk, offset);
            if (readCount <= 0) {
                //If no content is read we end the loop
                done = true;
            }
            numberOfBytesWritten = writeBytes(destinationChannel, readContent, offset, readCount);
        }
    } catch (error err) {
        throw err;
    }
}

@Description {value:"This function reads the specified number of bytes from the given channel."}
function readBytes (io:ByteChannel channel, int numberOfBytes, int offset) (blob, int) {
    blob bytes;
    int numberOfBytesRead;
    io:IOError readError;
    // Here is how the bytes are read from the channel.
    bytes, numberOfBytesRead, readError = channel.read(numberOfBytes, offset);
    if (readError != null) {
        throw readError.cause;
    }
    return bytes, numberOfBytesRead;
}

@Description {value:"This function writes a byte content with the given offset to a channel."}
function writeBytes (io:ByteChannel channel, blob content, int startOffset, int size) (int) {
    int numberOfBytesWritten;
    io:IOError writeError;
    // Here is how the bytes are written to the channel.
    numberOfBytesWritten, writeError = channel.write(content, startOffset, size);
    if (writeError != null) {
        throw writeError.cause;
    }
    return numberOfBytesWritten;
}

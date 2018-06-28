import ballerina/file;
import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

// Creating an endpoint for the client.
endpoint http:Client clientEP {
    url: "http://localhost:9092"
};

// Creating a listener for the service.
endpoint http:Listener multipartEP {
    port: 9090
};

@http:ServiceConfig { basePath: "/multiparts" }
service<http:Service> multipartResponseEncoder bind { port: 9092 } {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/encode_out_response"
    }
    multipartSender(endpoint caller, http:Request request) {

        // Create an enclosing entity to hold child parts.
        mime:Entity parentPart = new;

        // Create a child part with json content.
        mime:Entity childPart1 = new;
        childPart1.setJson({ "name": "wso2" });

        // Create another child part with a file.
        mime:Entity childPart2 = new;
        // This file path is relative to where the ballerina is running.
        //If your file is located outside, please give the
        //absolute file path instead.
        childPart2.setFileAsEntityBody("./files/test.xml",
            contentType = mime:TEXT_XML);
        // Create an array to hold child parts.
        mime:Entity[] childParts = [childPart1, childPart2];

        // Set the child parts to the parent part.
        parentPart.setBodyParts(childParts,
            contentType = mime:MULTIPART_MIXED);

        // Create an array to hold the parent part and set it to response.
        mime:Entity[] immediatePartsToResponse = [parentPart];
        http:Response outResponse = new;
        outResponse.setBodyParts(immediatePartsToResponse,
            contentType = mime:MULTIPART_FORM_DATA);

        caller->respond(outResponse) but {
            error e => log:printError("Error in responding ", err = e) };
    }
}

// Binding the listener to the service.
@http:ServiceConfig { basePath: "/multiparts" }
service multipartResponseDecoder bind multipartEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/decode_in_response"
    }
    // This resource accepts multipart responses.
    multipartReceiver(endpoint caller, http:Request request) {
        http:Response inResponse = new;
        // Extract the bodyparts from the response.
        var returnResult = clientEP->get("/multiparts/encode_out_response");
        http:Response res = new;
        match returnResult {
            // Setting the error response in-case of an error
            error connectionErr => {
                res.statusCode = 500;
                res.setPayload("Connection error");
            }
            http:Response returnResponse => {
                match returnResponse.getBodyParts() {
                    error err => {
                        res.statusCode = 500;
                        res.setPayload(err.message);
                    }
                    mime:Entity[] parentParts => {
                        //Loop through body parts.
                        foreach parentPart in parentParts {
                            handleNestedParts(parentPart);
                        }
                        res.setPayload("Body Parts Received!");
                    }
                }
            }
        }
        caller->respond(res) but {
            error e => log:printError("Error in responding ", err = e) };
    }
}

// Get the child parts that are nested within a parent.
function handleNestedParts(mime:Entity parentPart) {
    string contentTypeOfParent = parentPart.getContentType();
    if (contentTypeOfParent.hasPrefix("multipart/")) {
        match parentPart.getBodyParts() {
            error err => {
                log:printError("Error retrieving child parts! " + err.message);
            }
            mime:Entity[] childParts => {
                int i = 0;
                log:printInfo("Nested Parts Detected!");
                foreach childPart in childParts {
                    handleContent(childPart);
                }
            }
        }
    }
}

// The content logic that handles the body parts
//vary based on your requirement.
function handleContent(mime:Entity bodyPart) {
    mime:MediaType mediaType = check
    mime:getMediaType(bodyPart.getContentType());
    string baseType = mediaType.getBaseType();
    if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
        // Extract xml data from body part and print.
        var payload = bodyPart.getXml();
        match payload {
            error err =>
            log:printError("Error in getting xml payload :" + err.message);
            xml xmlContent => log:printInfo(<string>xmlContent);
        }
    } else if (mime:APPLICATION_JSON == baseType) {
        // Extract json data from body part and print.
        var payload = bodyPart.getJson();
        match payload {
            error err => log:printError("Error in getting json payload :"
                    + err.message);
            json jsonContent => log:printInfo(jsonContent.toString());
        }
    } else if (mime:TEXT_PLAIN == baseType) {
        // Extract text data from body part and print.
        var payload = bodyPart.getText();
        match payload {
            error err => log:printError("Error in getting string payload :"
                    + err.message);
            string textContent => log:printInfo(textContent);
        }
    } else if (mime:APPLICATION_PDF == baseType) {
        var payload = bodyPart.getByteChannel();
        match payload {
            error err => log:printError("Error in getting byte channel :"
                    + err.message);
            io:ByteChannel byteChannel => {
                io:ByteChannel destinationChannel =
                    getFileChannel("ReceivedFile.pdf", io:WRITE);
                try {
                    copy(byteChannel, destinationChannel);
                    log:printInfo("File Received");
                } catch (error err) {
                    log:printError("error occurred while saving file : "
                            + err.message);
                } finally {
                    // Close the created connections.
                    byteChannel.close() but {
                        error e => log:printError("Error closing byteChannel ",
                            err = e) };
                    destinationChannel.close() but {
                        error e =>
                        log:printError("Error closing destinationChannel",
                            err = e)
                    };
                }
            }
        }
    }
}

// This function returns a ByteChannel from a given file location according
// to the specified file permission
//(i.e., whether the file should be opened for read or write)."}
function getFileChannel(string filePath, io:Mode permission)
             returns (io:ByteChannel) {
    // Here is how the ByteChannel is retrieved from the file.
    io:ByteChannel channel = io:openFile(filePath, permission);
    return channel;
}

// This function reads a specified number of bytes from the given channel.
function readBytes(io:ByteChannel channel, int numberOfBytes)
             returns (blob, int) {

    // Here is how the bytes are read from the channel.
    var result = channel.read(numberOfBytes);
    match result {
        (blob, int) content => {
            return content;
        }
        error readError => {
            throw readError;
        }
    }
}

// This function writes a byte content with the given offset to a channel.
function writeBytes(io:ByteChannel channel, blob content, int startOffset = 0)
             returns (int) {

    // Here is how the bytes are written to the channel.
    var result = channel.write(content, startOffset);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        error err => {
            throw err;
        }
    }
}

// This function copies content from the source channel to a
//destination channel.
function copy(io:ByteChannel src, io:ByteChannel dst) {
    // Specifies the number of bytes that should be read from a
    //single read operation.
    int bytesChunk = 10000;
    int numberOfBytesWritten = 0;
    int readCount = 0;
    int offset = 0;
    blob readContent;
    boolean doneCopying = false;
    try {
        // Here is how to read all the content from
        // the source and copy it to the destination.
        while (!doneCopying) {
            (readContent, readCount) = readBytes(src, 1000);
            if (readCount <= 0) {
                //If no content is read, the loop is ended.
                doneCopying = true;
            }
            numberOfBytesWritten = writeBytes(dst, readContent);
        }
    } catch (error err) {
        throw err;
    }
}


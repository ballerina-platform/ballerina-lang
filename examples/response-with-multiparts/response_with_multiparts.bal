import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

// Creates an endpoint for the client.
http:Client clientEP = new("http://localhost:9092");

// Creates a listener for the service.
listener http:Listener multipartEP = new(9090);

@http:ServiceConfig {
    basePath: "/multiparts"
}
service multipartResponseEncoder on new http:Listener(9092) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/encode_out_response"
    }
    resource function multipartSender(http:Caller caller,
                                        http:Request request) {
        // Creates an enclosing entity to hold the child parts.
        mime:Entity parentPart = new;

        // Creates a child part with the JSON content.
        mime:Entity childPart1 = new;
        childPart1.setJson({ "name": "wso2" });
        // Creates another child part with a file.
        mime:Entity childPart2 = new;
        // This file path is relative to where the Ballerina is running.
        //If your file is located outside, please give the
        //absolute file path instead.
        childPart2.setFileAsEntityBody("./files/test.xml",
            contentType = mime:TEXT_XML);
        // Creates an array to hold the child parts.
        mime:Entity[] childParts = [childPart1, childPart2];
        // Sets the child parts to the parent part.
        parentPart.setBodyParts(childParts,
            contentType = mime:MULTIPART_MIXED);
        // Creates an array to hold the parent part and set it to the response.
        mime:Entity[] immediatePartsToResponse = [parentPart];
        http:Response outResponse = new;
        outResponse.setBodyParts(immediatePartsToResponse,
            contentType = mime:MULTIPART_FORM_DATA);
        var result = caller->respond(outResponse);
        if (result is error) {
            log:printError("Error in responding ", err = result);
        }
    }
}

// Binds the listener to the service.
@http:ServiceConfig {
    basePath: "/multiparts"
}
service multipartResponseDecoder on multipartEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/decode_in_response"
    }
    // This resource accepts multipart responses.
    resource function multipartReceiver(http:Caller caller,
                                        http:Request request) {
        http:Response inResponse = new;
        var returnResult = clientEP->get("/multiparts/encode_out_response");
        http:Response res = new;
        if (returnResult is http:Response) {
            // Extracts the bodyparts from the response.
            var parentParts = returnResult.getBodyParts();
            if (parentParts is mime:Entity[]) {
                //Loops through body parts.
                foreach var parentPart in parentParts {
                    handleNestedParts(parentPart);
                }
                res.setPayload("Body Parts Received!");
            }
        } else {
            res.statusCode = 500;
            res.setPayload("Connection error");
        }
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error in responding ", err = result);
        }
    }
}

// Gets the child parts that are nested within the parent.
function handleNestedParts(mime:Entity parentPart) {
    string contentTypeOfParent = parentPart.getContentType();
    if (contentTypeOfParent.hasPrefix("multipart/")) {
        var childParts = parentPart.getBodyParts();
        if (childParts is mime:Entity[]) {
            log:printInfo("Nested Parts Detected!");
            foreach var childPart in childParts {
                handleContent(childPart);
            }
        } else {
            log:printError("Error retrieving child parts! " +
                            string.convert(childParts.detail().message));
        }
    }
}

//The content logic that handles the body parts
//vary based on your requirement.
function handleContent(mime:Entity bodyPart) {
    string baseType = getBaseType(bodyPart.getContentType());
    if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
        // Extracts `xml` data from the body part.
        var payload = bodyPart.getXml();
        if (payload is xml) {
            string strValue = io:sprintf("%s", payload);
             log:printInfo("XML data: " + strValue);
        } else {
             log:printError("Error in parsing XML data", err = payload);
        }
    } else if (mime:APPLICATION_JSON == baseType) {
        // Extracts `json` data from the body part.
        var payload = bodyPart.getJson();
        if (payload is json) {
            log:printInfo("JSON data: " + payload.toString());
        } else {
             log:printError("Error in parsing JSON data", err = payload);
        }
    } else if (mime:TEXT_PLAIN == baseType) {
        // Extracts text data from the body part.
        var payload = bodyPart.getText();
        if (payload is string) {
            log:printInfo("Text data: " + payload);
        } else {
            log:printError("Error in parsing text data", err = payload);
        }
    } else if (mime:APPLICATION_PDF == baseType) {
        //Extracts byte channel from the body part and save it as a file.
        var payload = bodyPart.getByteChannel();
        if (payload is io:ReadableByteChannel) {
            io:WritableByteChannel destinationChannel =
                                    io:openWritableFile("ReceivedFile.pdf");
            var result = copy(payload, destinationChannel);
            if (result is error) {
                log:printError("error occurred while performing copy ",
                                err = result);
            }
            close(payload);
            close(destinationChannel);
        } else {
            log:printError("Error in parsing byte channel :", err = payload);
        }
    }
}

//Gets the base type from a given content type.
function getBaseType(string contentType) returns string {
    var result = mime:getMediaType(contentType);
    if (result is mime:MediaType) {
        return result.getBaseType();
    } else {
        panic result;
    }
}

// Copies the content from the source channel to the destination channel.
function copy(io:ReadableByteChannel src, io:WritableByteChannel dst)
                returns error? {
    int readCount = 1;
    byte[] readContent;
    while (readCount > 0) {
    //Operation attempts to read a maximum of 1000 bytes.
    (byte[], int) result = check src.read(1000);
    (readContent, readCount) = result;
    //Writes the given content into the channel.
    var writeResult = check dst.write(readContent, 0);
    }
    return;
}

//Closes the byte channel.
function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    abstract object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}

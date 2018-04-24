import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

@http:ServiceConfig {basePath:"/nestedparts"}
service<http:Service> test bind {port:9090} {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/decoder"
    }
    nestedPartReceiver(endpoint caller, http:Request req) {
        http:Response res = new;
        match req.getBodyParts() {
            error err => {
                res.setPayload(err.message);
                res.statusCode = 500;
            }
            mime:Entity[] parentParts => {
                foreach parentPart in parentParts {
                    handleNestedParts(parentPart);
                }
                res.setPayload("Nested Parts Received!");
            }
        }
        caller->respond(res) but { error e => log:printError("Error in responding ", err = e) };
    }
}

// This function retrieves the child parts of the specified parent part.
function handleNestedParts(mime:Entity parentPart) {
    string contentTypeOfParent = parentPart.getContentType();
    if (contentTypeOfParent.hasPrefix("multipart/")) {
        match parentPart.getBodyParts() {
            error err => {
                log:printInfo("Error retrieving child parts!");
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

// The logic depending on the format in which you want to retrieve body part content.
function handleContent(mime:Entity bodyPart) {
    string baseType = check mime:getMediaType(bodyPart.getContentType())!getBaseType();
    if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
        // Extract xml data from the body part and print the content.
        var payload = bodyPart.getXml();
        match payload {
            error err => log:printInfo("Error in getting xml payload");
            xml xmlContent => io:println(xmlContent);
        }
    } else if (mime:APPLICATION_JSON == baseType) {
        // Extract json data from the body part and print the content.
        var payload = bodyPart.getJson();
        match payload {
            error err => log:printInfo("Error in getting json payload");
            json jsonContent => io:println(jsonContent);
        }
    } else if (mime:TEXT_PLAIN == baseType) {
        // Extract text data from the body part and print the content.
        var payload = bodyPart.getText();
        match payload {
            error err => log:printInfo("Error in getting string payload");
            string textContent => log:printInfo(textContent);
        }
    }
}

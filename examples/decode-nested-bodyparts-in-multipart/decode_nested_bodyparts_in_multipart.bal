import ballerina/http;
import ballerina/mime;
import ballerina/io;

endpoint http:Listener multipartEP {
    port: 9090
};

@http:ServiceConfig {basePath: "/nestedparts"}
service<http:Service> test bind multipartEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/decoder"
    }
    nestedPartReceiver(endpoint conn, http:Request req) {
        http:Response res = new;
        match req.getBodyParts() {
            mime:EntityError err => {
                res.setStringPayload("Error occurred while decoding parent parts!");
                res.statusCode = 500;
            }
            mime:Entity[] parentParts => {
                int i = 0;
                while (i < lengthof parentParts) {
                    mime:Entity parentPart = parentParts[i];
                    handleNestedParts(parentPart);
                    i = i + 1;
                }
                res.setStringPayload("Nested Parts Received!");
            }
        }
        _ = conn->respond(res);
    }
}

//This function retrieves the child parts of the specified parent part.
function handleNestedParts(mime:Entity parentPart) {
    string contentTypeOfParent = parentPart.getContentType();
    if (contentTypeOfParent.hasPrefix("multipart/")) {
        match parentPart.getBodyParts() {
            mime:EntityError err => {
                io:println("Error retrieving child parts!");
            }
            mime:Entity[] childParts => {
                int i = 0;
                io:println("Nested Parts Detected!");
                while (i < lengthof childParts) {
                    mime:Entity childPart = childParts[i];
                    handleContent(childPart);
                    i = i + 1;
                }
            }
        }
    }
}

@Description {value: "The logic depending on the format in which you want to retrieve body part content."}
function handleContent(mime:Entity bodyPart) {
    string baseType = check mime:getMediaType(bodyPart.getContentType())!getBaseType();
    if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
        //Extract xml data from the body part and print the content.
        var payload = bodyPart.getXml();
        match payload {
            mime:EntityError err => io:println("Error in getting xml payload");
            xml xmlContent => io:println(xmlContent);
        }
    } else if (mime:APPLICATION_JSON == baseType) {
        //Extract json data from the body part and print the content.
        var payload = bodyPart.getJson();
        match payload {
            mime:EntityError err => io:println("Error in getting json payload");
            json jsonContent => io:println(jsonContent);
        }
    } else if (mime:TEXT_PLAIN == baseType) {
        //Extract text data from the body part and print the content.
        var payload = bodyPart.getText();
        match payload {
            mime:EntityError err => io:println("Error in getting string payload");
            string textContent => io:println(textContent);
        }
    }
}

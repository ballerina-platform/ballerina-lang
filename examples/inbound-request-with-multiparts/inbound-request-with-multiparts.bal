import ballerina/http;
import ballerina/mime;
import ballerina/io;

// Creating a listener for the service.
endpoint http:Listener multipartEP {
    port:9090
};

// Binding the listener to the service.
@http:ServiceConfig {basePath:"/multiparts"}
service<http:Service> test bind multipartEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/decode_in_request"
    }
    // This resource accepts multipart requests.
    receiveMultiparts (endpoint conn, http:Request request) {
        http:Response response = new;
        // Extract the bodyparts from the request.
        match request.getBodyParts() {
        // Setting the error response in case of an error
            mime:EntityError err => {
                io:println(err);
                response.setStringPayload("Error in decoding multiparts!");
                response.statusCode = 500;
            }
            // Iterate through the body parts.
            mime:Entity[] bodyParts => {
                int i = 0;
                io:println("Body Parts Detected!");
                while (i < lengthof bodyParts) {
                    mime:Entity part = bodyParts[i];
                    handleContent(part);
                    i = i + 1;
                }
                response.setStringPayload("Multiparts Received!");
            }
        }
        _ = conn -> respond(response);
    }
}

@Description {value:"The content logic that handles the body parts vary based on your requirement."}
function handleContent (mime:Entity bodyPart) {
    string baseType = bodyPart.contentType.getBaseType();
    if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
        //Extract the xml data from the body part and print it.
        var payload = bodyPart.getXml();
        match payload {
            mime:EntityError err => io:println("Error in getting xml payload");
            xml xmlContent => io:println(xmlContent);
        }
    } else if (mime:APPLICATION_JSON == baseType) {
        //Extract the json data from the body part and print it.
        var payload = bodyPart.getJson();
        match payload {
            mime:EntityError err => io:println("Error in getting json payload");
            json jsonContent => io:println(jsonContent);
        }
    } else if (mime:TEXT_PLAIN == baseType) {
        //Extract the text data from the body part and print it.
        var payload = bodyPart.getText();
        match payload {
            mime:EntityError err => io:println("Error in getting string payload");
            string textContent => io:println(textContent);
        }
    }
}

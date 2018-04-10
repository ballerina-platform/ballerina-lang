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
        //Extract the bodyparts from the request.
        match request.getMultiparts() {
        // Setting the error response in-case of an error
            mime:EntityError err => {
                io:println(err);
                response.setStringPayload("Error in decoding multiparts!");
                response.statusCode = 500;
            }
            // Iterate through the bodyparts.
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

@Description {value:"The body part content logic handling varies based on your requirement. "}
function handleContent (mime:Entity bodyPart) {
    string contentType = bodyPart.contentType.toString();
    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
        //Extract xml data from body part and print.
        var payload = bodyPart.getXml();
        match payload {
            mime:EntityError err => io:println("Error in getting xml payload");
            xml xmlContent => io:println(xmlContent);
        }
    } else if (mime:APPLICATION_JSON == contentType) {
        //Extract json data from body part and print.
        var payload = bodyPart.getJson();
        match payload {
            mime:EntityError err => io:println("Error in getting json payload");
            json jsonContent => io:println(jsonContent);
        }
    } else if (mime:TEXT_PLAIN == contentType) {
        //Extract text data from body part and print.
        var payload = bodyPart.getText();
        match payload {
            mime:EntityError err => io:println("Error in getting string payload");
            string textContent => io:println(textContent);
        }
    }
}

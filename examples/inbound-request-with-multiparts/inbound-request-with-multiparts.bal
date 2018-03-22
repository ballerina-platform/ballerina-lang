import ballerina/net.http;
import ballerina/mime;
import ballerina/io;

endpoint http:ServiceEndpoint mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/multiparts"}
service<http:Service> test bind mockEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/decode_in_request"
    }
    receiveMultiparts (endpoint conn, http:Request request) {
        http:Response response = {};
        match request.getMultiparts() {
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
            mime:EntityError err => {
                io:println("Error");
            }
        }
        _ = conn -> respond(response);
    }
}

@Description {value:"Handling body part content logic varies according to user's requirement.."}
function handleContent (mime:Entity bodyPart) {
    string contentType = bodyPart.contentType.toString();
    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
        //Extract xml data from body part and print.
        var xmlContent =? bodyPart.getXml();
        io:println(xmlContent);
    } else if (mime:APPLICATION_JSON == contentType) {
        //Extract json data from body part and print.
        var jsonContent =? bodyPart.getJson();
        io:println(jsonContent);
    } else if (mime:TEXT_PLAIN == contentType) {
        //Extract text data from body part and print.
        var textContent =? bodyPart.getText();
        io:println(textContent);
    }
}

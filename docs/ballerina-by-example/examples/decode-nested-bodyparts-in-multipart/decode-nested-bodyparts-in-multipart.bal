import ballerina.net.http;
import ballerina.mime;
import ballerina.io;

endpoint http:ServiceEndpoint mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/nestedparts"}
service<http:Service> test bind mockEP {
    @http:ResourceConfig {
      methods:["POST"],
      path:"/decoder"
    }
    nestedPartReceiver (endpoint conn, http:Request req)  {
        http:Response res = {};
        match request.getMultiparts() {
            mime:Entity[] parentParts => {
                int i = 0;
                while (i < lengthof parentParts) {
                    mime:Entity parentPart = parentParts[i];
                    handleNestedParts(parentPart);
                    i = i + 1;
                }
                res.setStringPayload("Nested Parts Received!");
            }
            mime:EntityError err => {
                res.setStringPayload("Error!");
                res.statusCode = 500;
            }
        }
        _ = conn -> respond(res);
    }
}

//Given a parent part, get it's child parts.
function handleNestedParts (mime:Entity parentPart) {

    match parentPart.getBodyParts() {
        mime:Entity[] childParts => {
            int i = 0;
            io:println("Nested Parts Detected!");
            while (i < lengthof childParts) {
                mime:Entity childPart = childParts[i];
                handleContent(childPart);
                i = i + 1;
            }
        }
        mime:EntityError err => {
            io:println("Error in child part!");
        }
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

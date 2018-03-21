import ballerina.net.http;
import ballerina.mime;
import ballerina.io;

endpoint<http:Client> clientEP {
    serviceUri: "http://localhost:9092"
}

endpoint http:ServiceEndpoint mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/multiparts"}
service<http:Service> test bind mockEP {
@http:ResourceConfig {
        methods:["GET"],
        path:"/decode_in_response"
    }
     receiveMultiparts (http:ServerConnector conn, http:Request request) {
        http:Request outRequest = {};
        http:Response inResponse = {};
        inResponse, _ = clientEP -> get("/multiparts/encode_out_response", outRequest);
        var parentParts =? inResponse.getMultiparts();
        http:Response res = {};
        if (payloadError == null) {
            int i = 0;
            //Loop through parent parts.
            while (i < lengthof parentParts) {
                mime:Entity parentPart = parentParts[i];
                handleNestedParts(parentPart);
                i = i + 1;
            }
            res.setStringPayload("Nested Parts Received!");
        } else {
            res.statusCode = 500;
            res.setStringPayload(payloadError.message);
        }

        _ = conn -> respond(res);
    }
}

//Given a parent part, get it's child parts.
function handleNestedParts (mime:Entity parentPart) {
    var childParts =? parentPart.getBodyParts();
    int i = 0;
    if (childParts != null) {
        io:println("Nested Parts Detected!");
        while (i < lengthof childParts) {
            mime:Entity childPart = childParts[i];
            handleContent(childPart);
            i = i + 1;
        }
    } else {
        //When there are no nested parts in a body part, handle the body content directly.
        io:println("Parent doesn't have children. So handling the body content directly...");
        handleContent(parentPart);
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

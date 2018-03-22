import ballerina/net.http;
import ballerina/mime;
import ballerina/io;

endpoint http:ClientEndpoint clientEP {
    targets:[{uri:"http://localhost:9092"}]
};

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
        var returnResult = clientEP -> get("/multiparts/encode_out_response", outRequest);
        http:Response res = {};
        match returnResult.getMultiparts() {
            mime:EntityError err => {
                res.statusCode = 500;
                res.setStringPayload(payloadError.message);
            }
            mime:Entity[] bodyParts => {
                int i = 0;
                //Loop through body parts.
                while (i < lengthof parentParts) {
                    mime:Entity bodyPart = bodyParts[i];
                    handleNestedParts(bodyPart);
                    i = i + 1;
                }
                res.setStringPayload("Body Parts Received!");
            }
        }
        _ = conn -> respond(res);
    }
}

@Description {value:"Handling body part content logic varies according to user's requirement.."}
function handleContent (mime:Entity bodyPart) {
    string contentType = bodyPart.contentType.toString();
    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
        //Extract xml data from body part and print.
        var payload = bodyPart.getXml();
        match payload {
            mime:EntityError err => io:println("Error in getting xml payload");
            xml xmlCotnent => io:println(xmlContent);
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

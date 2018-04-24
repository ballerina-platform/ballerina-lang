import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

endpoint http:Client clientEP {
    url:"http://localhost:9090"
};

@http:ServiceConfig {basePath:"/nestedparts"}
service<http:Service> test bind {port:9092} {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/encoder"
    }
    nestedPartSender(endpoint caller, http:Request req) {

        // Create an enclosing entity to hold child parts.
        mime:Entity parentPart = new;

        // Create a child part with json content.
        mime:Entity childPart1 = new;
        childPart1.setJson({"name":"wso2"});

        // Create another child part with a file.
        mime:Entity childPart2 = new;
        // The file path specified here is relative to where Ballerina is running. If your file is in a different location,
        // be sure to specify the absolute file path to your file.
        childPart2.setFileAsEntityBody("./files/test.xml", contentType = mime:APPLICATION_XML);

        // Create an array to hold child parts.
        mime:Entity[] childParts = [childPart1, childPart2];

        // Set the child parts to the parent part.
        parentPart.setBodyParts(childParts, contentType = mime:MULTIPART_MIXED);

        // Create an array to hold the parent part and set it to the request.
        mime:Entity[] immediatePartsToRequest = [parentPart];
        http:Request request = new;
        request.setBodyParts(immediatePartsToRequest, contentType = mime:MULTIPART_FORM_DATA);

        var returnResponse = clientEP->post("/nestedparts/decoder", request = request);
        match returnResponse {
            error err => {
                http:Response resp1 = new;
                resp1.setPayload("Error occurred while sending multipart request!");
                resp1.statusCode = 500;
                caller->respond(resp1) but { error e => log:printError("Error in responding ", err = e) };
            }
            http:Response returnResult => caller->respond(returnResult) but { error e => log:printError(
                                                "Error in responding ", err = e) };
        }
    }
}

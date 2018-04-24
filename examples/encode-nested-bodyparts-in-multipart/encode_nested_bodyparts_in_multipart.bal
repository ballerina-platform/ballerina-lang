import ballerina/http;
import ballerina/mime;
import ballerina/io;

endpoint http:Client clientEP {
    url: "http://localhost:9090"
};

endpoint http:Listener multipartEP {
    port: 9092
};

@http:ServiceConfig {basePath: "/nestedparts"}
service<http:Service> test bind multipartEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/encoder"
    }
    nestedPartSender(endpoint conn, http:Request req) {

        //Create an enclosing entity to hold child parts.
        mime:Entity parentPart = new;
        parentPart.setContentType(mime:MULTIPART_MIXED);

        //Create a child part with json content.
        mime:Entity childPart1 = new;
        childPart1.setContentType(mime:APPLICATION_JSON);
        childPart1.setJson({"name": "wso2"});

        //Create another child part with a file.
        mime:Entity childPart2 = new;
        childPart2.setContentType(mime:TEXT_XML);
        //The file path specified here is relative to where Ballerina is running. If your file is in a different location,
        //be sure to specify the absolute file path to your file.
        childPart2.setFileAsEntityBody("./files/test.xml");

        //Create an array to hold child parts.
        mime:Entity[] childParts = [childPart1, childPart2];

        //Set the child parts to the parent part.
        parentPart.setBodyParts(childParts);

        //Create an array to hold the parent part and set it to the request.
        mime:Entity[] immediatePartsToRequest = [parentPart];
        http:Request request = new;
        request.setBodyParts(immediatePartsToRequest, contentType = mime:MULTIPART_FORM_DATA);

        var returnResponse = clientEP->post("/nestedparts/decoder", request = request);
        match returnResponse {
            http:HttpConnectorError err => {
                http:Response resp1 = new;
                io:println(err);
                resp1.setStringPayload("Error occurred while sending multipart request!");
                resp1.statusCode = 500;
                _ = conn->respond(resp1);
            }
            http:Response returnResult => _ = conn->respond(returnResult);
        }
    }
}

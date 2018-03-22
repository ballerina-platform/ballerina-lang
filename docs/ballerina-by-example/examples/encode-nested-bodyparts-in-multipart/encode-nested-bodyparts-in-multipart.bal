import ballerina/net.http;
import ballerina/mime;
import ballerina/file;

endpoint http:ClientEndpoint clientEP {
    targets:[{uri:"http://localhost:9093"}]
};

endpoint http:ServiceEndpoint mockEP {
    port:9092
};

@http:ServiceConfig {basePath:"/nestedparts"}
service<http:Service> test bind mockEP {
      @http:ResourceConfig {
        methods:["POST"],
        path:"/encoder"
    }
    nestedPartSender (endpoint conn, http:Request req) {

        //Create an enclosing entity to hold child parts.
        mime:Entity parentPart = {};
        mime:MediaType mixedContentType = mime:getMediaType(mime:MULTIPART_MIXED);
        parentPart.contentType = mixedContentType;

        //Create a child part with json content.
        mime:Entity childPart1 = {};
        mime:MediaType contentTypeOfJsonPart = mime:getMediaType(mime:APPLICATION_JSON);
        childPart1.contentType = contentTypeOfJsonPart;
        childPart1.setJson({"name":"wso2"});

        //Create another child part with a file.
        mime:Entity childPart2 = {};
        mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:TEXT_XML);
        childPart2.contentType = contentTypeOfFilePart;
        //This file path is relative to where the ballerina is running. If your file is located outside, please
        //give the absolute file path instead.
        file:File fileHandler = {path:"./files/test.xml"};
        childPart2.setFileAsEntityBody(fileHandler);

        //Create an array to hold child parts.
        mime:Entity[] childParts = [childPart1, childPart2];

        //Set the child parts to parent part.
        parentPart.setBodyParts(childParts);

        //Create an array to hold the parent part and set it to request.
        mime:Entity[] immediatePartsToRequest = [parentPart];
        http:Request request = {};
        request.setMultiparts(immediatePartsToRequest, mime:MULTIPART_FORM_DATA);

        http:Response resp1 = {};
        var returnResponse = clientEP -> post("/nestedparts/decoder", request);
        match returnResponse {
            http:HttpConnectorError err => {
                io:println(err);
                http:Response resp1 ={};
                resp1.setStringPayload("Error response");
                _ = conn -> respond(resp1);
            }
            http:Response resp1 =>  _ = conn -> forward(resp1);
        }
    }
}

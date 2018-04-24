import ballerina/http;
import ballerina/mime;
import ballerina/file;

endpoint http:Listener multipartEP {
    port: 9092
};

@http:ServiceConfig {basePath: "/multiparts"}
service<http:Service> test bind multipartEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/encode_out_response"
    }
    multipartSender(endpoint conn, http:Request request) {

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
        //This file path is relative to where the ballerina is running. If your file is located outside, please
        //give the absolute file path instead.
        childPart2.setFileAsEntityBody("./files/test.xml");

        //Create an array to hold child parts.
        mime:Entity[] childParts = [childPart1, childPart2];

        //Set the child parts to the parent part.
        parentPart.setBodyParts(childParts);

        //Create an array to hold the parent part and set it to response.
        mime:Entity[] immediatePartsToResponse = [parentPart];
        http:Response outResponse = new;
        outResponse.setBodyParts(immediatePartsToResponse, contentType = mime:MULTIPART_FORM_DATA);

        _ = conn->respond(outResponse);
    }
}

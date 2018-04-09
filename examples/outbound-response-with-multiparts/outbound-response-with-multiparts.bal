import ballerina/http;
import ballerina/mime;
import ballerina/file;

endpoint http:ServiceEndpoint multipartEP {
    port:9092
};

@http:ServiceConfig {basePath:"/multiparts"}
service<http:Service> test bind multipartEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/encode_out_response"
    }
    multipartSender (endpoint conn, http:Request request) {

        //Create an enclosing entity to hold child parts.
        mime:Entity parentPart = new;
        mime:MediaType mixedContentType = mime:getMediaType(mime:MULTIPART_MIXED);
        parentPart.contentType = mixedContentType;

        //Create a child part with json content.
        mime:Entity childPart1 = new;
        mime:MediaType contentTypeOfJsonPart = mime:getMediaType(mime:APPLICATION_JSON);
        childPart1.contentType = contentTypeOfJsonPart;
        childPart1.setJson({"name":"wso2"});

        //Create another child part with a file.
        mime:Entity childPart2 = new;
        mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:TEXT_XML);
        childPart2.contentType = contentTypeOfFilePart;
        //This file path is relative to where the ballerina is running. If your file is located outside, please
        //give the absolute file path instead.
        file:Path fileHandler = file:getPath("./files/test.xml");
        childPart2.setFileAsEntityBody(fileHandler);

        //Create an array to hold child parts.
        mime:Entity[] childParts = [childPart1, childPart2];

        //Set the child parts to parent part.
        parentPart.setBodyParts(childParts);

        //Create an array to hold the parent part and set it to response.
        mime:Entity[] immediatePartsToResponse = [parentPart];
        http:Response outResponse = new;
        outResponse.setMultiparts(immediatePartsToResponse, mime:MULTIPART_FORM_DATA);

        _ = conn -> respond(outResponse);
    }
}

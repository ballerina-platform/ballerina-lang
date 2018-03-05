import ballerina.net.http;
import ballerina.mime;
import ballerina.file;

@http:configuration {port:9092}
service<http> nestedparts {
    @http:resourceConfig {
        methods:["POST"],
        path:"/encoder"
    }
    resource nestedPartSender (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> httpEndpoint {
            create http:HttpClient("http://localhost:9093", {});
        }

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
        file:File fileHandler = {path:"/home/user/Downloads/test.xml"};
        childPart2.setFileAsEntityBody(fileHandler);

        //Create an array to hold child parts.
        mime:Entity[] childParts = [childPart1, childPart2];

        //Set the child parts to parent part.
        parentPart.setBodyParts(childParts);

        //Create an array to hold the parent part and set it to request.
        mime:Entity[] immediatePartsToRequest = [parentPart];
        http:OutRequest request = {};
        request.setMultiparts(immediatePartsToRequest, mime:MULTIPART_FORM_DATA);

        http:InResponse resp1 = {};
        resp1, _ = httpEndpoint.post("/nestedparts/decoder", request);

        _ = conn.forward(resp1);
    }
}

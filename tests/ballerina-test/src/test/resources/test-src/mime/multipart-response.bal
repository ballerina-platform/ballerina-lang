import ballerina/http;
import ballerina/http;
import ballerina/mime;
import ballerina/file;

endpoint http:NonListener mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/multipart"}
service<http:Service> test bind mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/encode_out_response"
    }
    multipartOutResponse (endpoint conn, http:Request request) {

        //Create a body part with json content.
        mime:Entity bodyPart1 = new;
        mime:MediaType contentTypeOfJsonPart = mime:getMediaType(mime:APPLICATION_JSON);
        bodyPart1.contentType = contentTypeOfJsonPart;
        bodyPart1.setJson({"bodyPart":"jsonPart"});

        //Create another body part with a xml file.
        mime:Entity bodyPart2 = new;
        mime:MediaType textXml = mime:getMediaType(mime:TEXT_XML);
        bodyPart2.contentType = textXml;
        file:Path fileHandler = new("src/test/resources/datafiles/mime/file.xml");
        bodyPart2.setFileAsEntityBody(fileHandler);

        //Create a text body part.
        mime:Entity bodyPart3 = new;
        mime:MediaType contentTypeOfTextPart = mime:getMediaType(mime:TEXT_PLAIN);
        bodyPart3.contentType = contentTypeOfTextPart;
        bodyPart3.setText("Ballerina text body part");

        //Create another body part with a text file.
        mime:Entity bodyPart4 = new;
        mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
        bodyPart4.contentType = contentTypeOfFilePart;
        file:Path textFile = new("src/test/resources/datafiles/mime/test.tmp");
        bodyPart4.setFileAsEntityBody(textFile);

        //Create an array to hold all the body parts.
        mime:Entity[] bodyParts = [bodyPart1, bodyPart2, bodyPart3, bodyPart4];

        //Set the body parts to outbound response.
        http:Response outResponse = new;
        string contentType = mime:MULTIPART_MIXED + "; boundary=e3a0b9ad7b4e7cdb";
        outResponse.setMultiparts(bodyParts, contentType);

        _ = conn -> respond(outResponse);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/nested_parts_in_outresponse"
    }
    nestedPartsInOutResponse (endpoint conn, http:Request request) {
        string contentType = request.getHeader("content-type");
        http:Response outResponse = new;
        match (request.getMultiparts()) {
            mime:EntityError err => {
                outResponse.setStringPayload(err.message);
            }
            mime:Entity[] bodyParts => {
                outResponse.setMultiparts(bodyParts, contentType);
            }
        }
        _ = conn -> respond(outResponse);
    }
}

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
        bodyPart1.setJson({"bodyPart":"jsonPart"});

        //Create another body part with a xml file.
        mime:Entity bodyPart2 = new;
        bodyPart2.setFileAsEntityBody("src/test/resources/datafiles/mime/file.xml", contentType = mime:TEXT_XML);

        //Create a text body part.
        mime:Entity bodyPart3 = new;
        bodyPart3.setText("Ballerina text body part");

        //Create another body part with a text file.
        mime:Entity bodyPart4 = new;
        bodyPart4.setFileAsEntityBody("src/test/resources/datafiles/mime/test.tmp");

        //Create an array to hold all the body parts.
        mime:Entity[] bodyParts = [bodyPart1, bodyPart2, bodyPart3, bodyPart4];

        //Set the body parts to outbound response.
        http:Response outResponse = new;
        string contentType = mime:MULTIPART_MIXED + "; boundary=e3a0b9ad7b4e7cdb";
        outResponse.setBodyParts(bodyParts, contentType = contentType);

        _ = conn -> respond(outResponse);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/nested_parts_in_outresponse"
    }
    nestedPartsInOutResponse (endpoint conn, http:Request request) {
        string contentType = request.getHeader("content-type");
        http:Response outResponse = new;
        match (request.getBodyParts()) {
            mime:EntityError err => {
                outResponse.setStringPayload(err.message);
            }
            mime:Entity[] bodyParts => {
                outResponse.setBodyParts(bodyParts, contentType = contentType);
            }
        }
        _ = conn -> respond(outResponse);
    }
}

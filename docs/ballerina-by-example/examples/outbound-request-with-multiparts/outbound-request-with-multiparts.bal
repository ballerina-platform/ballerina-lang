import ballerina.net.http;
import ballerina.mime;
import ballerina.file;


@http:configuration {basePath:"/foo", port:9092}
service<http> echo {
    @http:resourceConfig {
        methods:["POST"],
        path:"/multiparts"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> httpEndpoint {
            create http:HttpClient("http://localhost:9090", {});
        }
        //Create a top level entity to hold body parts
        mime:Entity topLevelEntity = {};
        mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_FORM_DATA);
        topLevelEntity.contentType = mediaType;

        //Create a xml body part
        mime:Entity xmlBodyPart = {};
        xmlBodyPart.xmlData = xml `<name>Ballerina</name>`;
        mime:MediaType contentType = mime:getMediaType(mime:APPLICATION_XML);
        xmlBodyPart.contentType = contentType;
        xmlBodyPart.name="xml part";

        //Create a xml body part as a file upload
        mime:Entity xmlFilePart = {};
        mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_XML);
        xmlFilePart.contentType = contentTypeOfFilePart;
        file:File content = {path:"/home/user/Downloads/samplefile.xml"};
        xmlFilePart.overflowData = content;
        xmlFilePart.name="file part";

        //Create a json body part
        mime:Entity jsonBodyPart = {};
        jsonBodyPart.jsonData = "{'name':'wso2'}";
        mime:MediaType contentTypeOfJsonPart = mime:getMediaType(mime:APPLICATION_JSON);
        jsonBodyPart.contentType = contentTypeOfJsonPart;
        jsonBodyPart.name="json part";

        //Create an array to hold all the body parts
        mime:Entity[] bodyParts = [xmlBodyPart, xmlFilePart, jsonBodyPart];
        //Set the array of body parts to the top level entity
        topLevelEntity.multipartData =bodyParts;

        //Set the top level entity to outbound request
        http:OutRequest request = {};
        request.setEntity(topLevelEntity);

        //Send the multipart request
        http:InResponse resp1 = {};
        resp1, _ = httpEndpoint.post("/foo/receivableParts", request);

        //Forward the response back to the user
        _ = conn.forward(resp1);
    }
}

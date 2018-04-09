import ballerina/http;
import ballerina/mime;
import ballerina/file;
import ballerina/io;

endpoint http:ClientEndpoint clientEP {
    targets:[{url:"http://localhost:9090"}]
};

endpoint http:ServiceEndpoint multipartEP {
    port:9092
};

@http:ServiceConfig {basePath:"/multiparts"}
service<http:Service> test bind multipartEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/encoder"
    }
    encodeMultiparts (endpoint conn, http:Request req) {

        //Create a json body part.
        mime:Entity jsonBodyPart = new;
        mime:MediaType contentTypeOfJsonPart = mime:getMediaType(mime:APPLICATION_JSON);
        jsonBodyPart.contentType = contentTypeOfJsonPart;
        jsonBodyPart.contentDisposition = getContentDispositionForFormData("json part");
        jsonBodyPart.setJson({"name":"wso2"});

        //Create a xml body part as a file upload.
        mime:Entity xmlFilePart = new;
        mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:TEXT_XML);
        xmlFilePart.contentType = contentTypeOfFilePart;
        xmlFilePart.contentDisposition = getContentDispositionForFormData("xml file part");
        //This file path is relative to where the ballerina is running. If your file is located outside, please
        //give the absolute file path instead.
        file:Path fileHandler = file:getPath("./files/test.xml");
        xmlFilePart.setFileAsEntityBody(fileHandler);

        //Create a xml body part.
        mime:Entity xmlBodyPart = new;
        mime:MediaType contentType = mime:getMediaType(mime:APPLICATION_XML);
        xmlBodyPart.contentType = contentType;
        xmlBodyPart.contentDisposition = getContentDispositionForFormData("xml part");
        xml xmlContent= xml `<name>Ballerina</name>`;
        xmlBodyPart.setXml(xmlContent);

        //Create an array to hold all the body parts.
        mime:Entity[] bodyParts = [xmlBodyPart, xmlFilePart, jsonBodyPart];

        http:Request request = new;
        //Set body parts to request. Here the content-type is set as multipart form data. This also works with any other
        //multipart media type. eg:- multipart/mixed, multipart/related etc... Just pass the content type that suit
        //your requirement.
        request.setMultiparts(bodyParts, mime:MULTIPART_FORM_DATA);
        var returnResponse = clientEP -> post("/multiparts/decode_in_request", request);
        match returnResponse {
            http:HttpConnectorError err => {
                http:Response resp1 = new;
                io:println(err);
                resp1.setStringPayload("Error occurred while sending multipart request!");
                resp1.statusCode = 500;
                _ = conn -> respond(resp1);
            }
            http:Response returnResult => {_ = conn -> forward(returnResult);}
        }
    }
}

function getContentDispositionForFormData(string partName) returns (mime:ContentDisposition){
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name =  partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

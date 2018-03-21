import ballerina/net.http;
import ballerina/mime;
import ballerina/file;

endpoint<http:Service> multipartEP {
    port:9092
}

endpoint<http:Client> clientEP {
    serviceUri: "http://localhost:9090"
}

@http:serviceConfig { endpoints:[multipartEP] }
service<http:Service> multiparts {
    @http:resourceConfig {
        methods:["POST"],
        path:"/encoder"
    }
    resource encodeMultiparts (http:ServerConnector conn, http:Request req) {

        //Create a json body part.
        mime:Entity jsonBodyPart = {};
        mime:MediaType contentTypeOfJsonPart = mime:getMediaType(mime:APPLICATION_JSON);
        jsonBodyPart.contentType = contentTypeOfJsonPart;
        jsonBodyPart.contentDisposition = getContentDispositionForFormData("json part");
        jsonBodyPart.setJson({"name":"wso2"});

        //Create a xml body part as a file upload.
        mime:Entity xmlFilePart = {};
        mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:TEXT_XML);
        xmlFilePart.contentType = contentTypeOfFilePart;
        xmlFilePart.contentDisposition = getContentDispositionForFormData("xml file part");
        //This file path is relative to where the ballerina is running. If your file is located outside, please
        //give the absolute file path instead.
        file:File fileHandler = {path:"./files/test.xml"};
        xmlFilePart.setFileAsEntityBody(fileHandler);

        //Create a xml body part.
        mime:Entity xmlBodyPart = {};
        mime:MediaType contentType = mime:getMediaType(mime:APPLICATION_XML);
        xmlBodyPart.contentType = contentType;
        xmlBodyPart.contentDisposition = getContentDispositionForFormData("xml part");
        xml xmlContent= xml `<name>Ballerina</name>`;
        xmlBodyPart.setXml(xmlContent);

        //Create an array to hold all the body parts.
        mime:Entity[] bodyParts = [xmlBodyPart, xmlFilePart, jsonBodyPart];

        http:Request request = {};
        //Set body parts to request. Here the content-type is set as multipart form data. This also works with any other
        //multipart media type. eg:- multipart/mixed, multipart/related etc... Just pass the content type that suit
        //your requirement.
        request.setMultiparts(bodyParts, mime:MULTIPART_FORM_DATA);

        http:Response resp1 = {};
        resp1, _ = clientEP -> post("/multiparts/decode_in_request", request);

        _ = conn -> forward(resp1);
    }
}

function getContentDispositionForFormData(string partName) (mime:ContentDisposition){
    mime:ContentDisposition contentDisposition = {};
    contentDisposition.name =  partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

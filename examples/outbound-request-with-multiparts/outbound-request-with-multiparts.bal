import ballerina/http;
import ballerina/mime;
import ballerina/io;

endpoint http:Client clientEP {
    url:"http://localhost:9090"
};

endpoint http:Listener multipartEP {
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
        jsonBodyPart.setContentType(mime:APPLICATION_JSON);
        jsonBodyPart.setContentDisposition(getContentDispositionForFormData("json part"));
        jsonBodyPart.setJson({"name":"wso2"});

        //Create an xml body part as a file upload.
        mime:Entity xmlFilePart = new;
        xmlFilePart.setContentType(mime:TEXT_XML);
        xmlFilePart.setContentDisposition(getContentDispositionForFormData("xml file part"));
        //This file path is relative to where the ballerina is running. If your file is located outside, please
        //give the absolute file path instead.
        xmlFilePart.setFileAsEntityBody("./files/test.xml");

        //Create an xml body part.
        mime:Entity xmlBodyPart = new;
        xmlBodyPart.setContentType(mime:APPLICATION_XML);
        xmlBodyPart.setContentDisposition(getContentDispositionForFormData("xml part"));
        xml xmlContent= xml `<name>Ballerina</name>`;
        xmlBodyPart.setXml(xmlContent);

        //Create an array to hold all the body parts.
        mime:Entity[] bodyParts = [xmlBodyPart, xmlFilePart, jsonBodyPart];

        http:Request request = new;
        // Set the body parts to the request. Here the content-type is set as multipart form data. This also works with
        // any other multipart media type. eg:- multipart/mixed, multipart/related etc. You need to pass the content
        // type that suit your requirement.
        request.setBodyParts(bodyParts, contentType=mime:MULTIPART_FORM_DATA);
        var returnResponse = clientEP -> post("/multiparts/decode_in_request", request=request);
        match returnResponse {
            http:HttpConnectorError err => {
                http:Response resp1 = new;
                io:println(err);
                resp1.setPayload("Error occurred while sending multipart request!");
                resp1.statusCode = 500;
                _ = conn -> respond(resp1);
            }
            http:Response returnResult => {_ = conn -> respond(returnResult);}
        }
    }
}

function getContentDispositionForFormData(string partName) returns (mime:ContentDisposition){
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name =  partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

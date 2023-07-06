import ballerina/http;
import ballerina/log;
import ballerina/mime;

http:Client clientEP = new ("http://localhost:9090");

@http:ServiceConfig {
    basePath: "/multiparts"
}
//Binds the listener to the service.
service multipartDemoService on new http:Listener(9090) {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/decode"
    }
    resource function multipartReceiver(http:Caller caller, http:Request
                                        request) {
        http:Response response = new;
        // [Extracts bodyparts](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Request.html#getBodyParts) from the request.
        var bodyParts = request.getBodyParts();
        if (bodyParts is mime:Entity[]) {
            foreach var part in bodyParts {
                handleContent(part);
            }
            response.setPayload(<@untainted>bodyParts);
        } else {
            log:printError(bodyParts.message());
            response.setPayload("Error in decoding multiparts!");
            response.statusCode = 500;
        }
        var result = caller->respond(response);
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/encode"
    }
    resource function multipartSender(http:Caller caller, http:Request req) {
        //Create a json body part.
        mime:Entity jsonBodyPart = new;
        jsonBodyPart.setContentDisposition(
                        getContentDispositionForFormData("json part"));
        jsonBodyPart.setJson({"name": "wso2"});
        //Create an `xml` body part as a file upload.
        mime:Entity xmlFilePart = new;
        xmlFilePart.setContentDisposition(
                       getContentDispositionForFormData("xml file part"));
        // This file path is relative to where the ballerina is running.
        // If your file is located outside, please
        // give the absolute file path instead.
        xmlFilePart.setFileAsEntityBody("./files/test.xml",
                                        contentType = mime:APPLICATION_XML);
        // Create an array to hold all the body parts.
        mime:Entity[] bodyParts = [jsonBodyPart, xmlFilePart];
        http:Request request = new;
        // [Set the body parts](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Request.html#setBodyParts) to the request.
        // Here the content-type is set as multipart form data.
        // This also works with any other multipart media type.
        // eg:- `multipart/mixed`, `multipart/related` etc.
        // You need to pass the content type that suit your requirement.
        request.setBodyParts(bodyParts, contentType = mime:MULTIPART_FORM_DATA);
        var returnResponse = clientEP->post("/multiparts/decode", request);
        if (returnResponse is http:Response) {
            var result = caller->respond(<@untainted>returnResponse);
            if (result is error) {
                log:printError("Error sending response", result);
            }
        } else {
            http:Response response = new;
            response.setPayload("Error occurred while sending multipart " +
                                    "request!");
            response.statusCode = 500;
            var result = caller->respond(response);
            if (result is error) {
                log:printError("Error sending response", result);
            }
        }
    }
}

// The content logic that handles the body parts vary based on your requirement.
function handleContent(mime:Entity bodyPart) {
    // [Get the media type](https://ballerina.io/swan-lake/learn/api-docs/ballerina/mime/functions.html#getMediaType) from the body part retrieved from the request.
    var mediaType = mime:getMediaType(bodyPart.getContentType());
    if (mediaType is mime:MediaType) {
        string baseType = mediaType.getBaseType();
        if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
            //[Extracts `xml` data](https://ballerina.io/swan-lake/learn/api-docs/ballerina/mime/classes/Entity.html#getXml) from the body part.
            var payload = bodyPart.getXml();
            if (payload is xml) {
                log:printInfo(payload.toString());
            } else {
                log:printError(payload.message());
            }
        } else if (mime:APPLICATION_JSON == baseType) {
            //[Extracts `json` data](https://ballerina.io/swan-lake/learn/api-docs/ballerina/mime/classes/Entity.html#getJson) from the body part.
            var payload = bodyPart.getJson();
            if (payload is json) {
                log:printInfo(payload.toJsonString());
            } else {
                log:printError(payload.message());
            }
        } else if (mime:TEXT_PLAIN == baseType) {
            //[Extracts text data](https://ballerina.io/swan-lake/learn/api-docs/ballerina/mime/classes/Entity.html#getText) from the body part.
            var payload = bodyPart.getText();
            if (payload is string) {
                log:printInfo(payload);
            } else {
                log:printError(payload.message());
            }
        }
    }
}

function getContentDispositionForFormData(string partName)
                                    returns (mime:ContentDisposition) {
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name = partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

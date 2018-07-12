import ballerina/http;
import ballerina/log;
import ballerina/mime;

endpoint http:Client clientEP {
    url: "http://localhost:9090"
};

@http:ServiceConfig {basePath: "/multiparts"}
// Binding the listener to the service.
service<http:Service> multipartDemoService bind {port: 9090} {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/decode"
    }
    multipartReceiver(endpoint caller, http:Request request) {
        http:Response response = new;
        // Extract the bodyparts from the request.
        match request.getBodyParts() {

            // Setting the error response in case of an error
            error err => {
                log:printError(err.message);
                response.setPayload("Error in decoding multiparts!");
                response.statusCode = 500;
            }

            // Iterate through the body parts.
            mime:Entity[] bodyParts => {
                int i = 0;
                while (i < lengthof bodyParts) {
                    mime:Entity part = bodyParts[i];
                    handleContent(part);
                    i = i + 1;
                }
                response.setBodyParts(untaint bodyParts);
            }
        }
        caller->respond(response) but {
            error e => log:printError("Error sending response", err = e) };
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/encode"
    }
    multipartSender(endpoint caller, http:Request req) {

        //Create a json body part.
        mime:Entity jsonBodyPart = new;
        jsonBodyPart.setContentDisposition(
                        getContentDispositionForFormData("json part"));
        jsonBodyPart.setJson({"name": "wso2"});

        //Create an xml body part as a file upload.
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
        // Set the body parts to the request.
        // Here the content-type is set as multipart form data.
        // This also works with any other multipart media type.
        // eg:- multipart/mixed, multipart/related etc.
        // You need to pass the content type that suit your requirement.
        request.setBodyParts(bodyParts, contentType = mime:MULTIPART_FORM_DATA);

        var returnResponse = clientEP->post("/multiparts/decode", request);
        match returnResponse {
            error err => {
                http:Response response = new;
                response.setPayload(
                            "Error occurred while sending multipart request!");
                response.statusCode = 500;
                caller->respond(response) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            http:Response returnResult => {caller->respond(returnResult) but {
                error e => log:printError("Error sending response", err = e) };
            }
        }
    }

}

// The content logic that handles the body parts vary based on your requirement.
function handleContent(mime:Entity bodyPart) {

    mime:MediaType mediaType = check mime:getMediaType(bodyPart.getContentType());
    string baseType = mediaType.getBaseType();
    if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
        //Extract the xml data from the body part and print it.
        var payload = bodyPart.getXml();
        match payload {
            error err => log:printError(err.message);
            xml xmlContent => log:printInfo(<string>xmlContent);
        }

    } else if (mime:APPLICATION_JSON == baseType) {
        //Extract the json data from the body part and print it.
        var payload = bodyPart.getJson();
        match payload {
            error err => log:printError(err.message);
            json jsonContent => log:printInfo(jsonContent.toString());
        }

    } else if (mime:TEXT_PLAIN == baseType) {
        //Extract the text data from the body part and print it.
        var payload = bodyPart.getText();

        match payload {
            error err => log:printError(err.message);
            string textContent => log:printInfo(textContent);
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

import ballerina/http;
import ballerina/io;
import ballerina/mime;

function setErrorResponse(http:Response response,  error err) {
    response.statusCode = 500;
    response.setPayload(untaint <string>err.detail().message);
}

listener http:MockListener mockEP = new(9090);

@http:ServiceConfig {basePath:"/test"}
service test on mockEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/textbodypart"
    }
    resource function multipart1(http:Caller caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            var result = bodyParts[0].getText();
            if (result is string) {
                mime:Entity entity = new;
                entity.setText(untaint result);
                response.setEntity(entity);
            } else {
                setErrorResponse(response, result);
            }
        }

        _ = caller->respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/jsonbodypart"
    }
    resource function multipart2(http:Caller caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            var result = bodyParts[0].getJson();
            if (result is json) {
                response.setJsonPayload(untaint result);
            } else {
                setErrorResponse(response, result);
            }
        }
        _ = caller->respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/xmlbodypart"
    }
    resource function multipart3(http:Caller caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            var result = bodyParts[0].getXml();
            if (result is xml) {
                response.setXmlPayload(untaint result);
            } else {
                setErrorResponse(response, result);
            }
        }
        _ = caller->respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/binarybodypart"
    }
    resource function multipart4(http:Caller caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            var result = bodyParts[0].getByteArray();
            if (result is byte[]) {
                response.setBinaryPayload(untaint result);
            } else {
                setErrorResponse(response, result);
            }
        }
        _ = caller->respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/multipleparts"
    }
    resource function multipart5(http:Caller caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            string content = "";
            int i = 0;
            while (i < bodyParts.length()) {
                mime:Entity part = bodyParts[i];
                content = content + " -- " + handleContent(part);
                i = i + 1;
            }
            response.setTextPayload(untaint content);
        }
        _ = caller->respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/emptyparts"
    }
    resource function multipart6(http:Caller caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            response.setPayload("Body parts detected!");
        } else {
            response.setPayload(untaint <string>bodyParts.detail().message);
        }
        _ = caller->respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/nestedparts"
    }
    resource function multipart7(http:Caller caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            string payload = "";
            int i = 0;
            while (i < bodyParts.length()) {
                mime:Entity part = bodyParts[i];
                payload = handleNestedParts(part);
                i = i + 1;
            }
            response.setTextPayload(untaint payload);
        }
        _ = caller->respond(untaint response);
    }
}

function handleNestedParts(mime:Entity parentPart) returns (string) {
    string content = "";
    string contentTypeOfParent = parentPart.getContentType();
    if (contentTypeOfParent.hasPrefix("multipart/")) {
        var childParts = parentPart.getBodyParts();
        if (childParts is mime:Entity[]) {
            int i = 0;
            while (i < childParts.length()) {
                mime:Entity childPart = childParts[i];
                content = content + handleContent(childPart);
                i = i + 1;
            }
        } else {
            return "Error decoding nested parts";
        }
    }
    return content;
}

function handleContent(mime:Entity bodyPart) returns (string) {
    var mediaType = mime:getMediaType(bodyPart.getContentType());
    if (mediaType is mime:MediaType) {
        string baseType = mediaType.getBaseType();
        if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
            var payload = bodyPart.getXml();
            if (payload is xml) {
                return payload.getTextValue();
            } else {
                return "Error in getting xml payload";
            }
        } else if (mime:APPLICATION_JSON == baseType) {
            var payload = bodyPart.getJson();
            if (payload is json) {
                return extractFieldValue(payload.bodyPart);
            } else {
                return "Error in getting json payload";
            }
        } else if (mime:TEXT_PLAIN == baseType) {
            var payload = bodyPart.getText();
            if (payload is string) {
                return payload;
            } else {
                return "Error in getting string payload";
            }
        } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
            var payload = bodyPart.getByteArray();
            if (payload is byte[]) {
                return mime:byteArrayToString(payload, mime:DEFAULT_CHARSET);
            } else {
                return "Error in getting byte[] payload";
            }
        }
    } else {
        return mediaType.reason();
    }
    return "";
}

//Keep this until there's a simpler way to get a string value out of a json
function extractFieldValue(json fieldValue) returns string {
    if (fieldValue is string) {
        return fieldValue;
    } else {
        return "error";
    }
}

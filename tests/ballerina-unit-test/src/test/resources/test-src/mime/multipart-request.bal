import ballerina/http;
import ballerina/io;
import ballerina/mime;

function setErrorResponse(http:Response response,  error err) {
    response.statusCode = 500;
    response.setPayload(untaint <string>err.detail().message);
}

endpoint http:NonListener mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/test"}
service<http:Service> test bind mockEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/textbodypart"
    }
    multipart1 (endpoint caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            var result = bodyParts[0].getText();
            if (result is string) {
                mime:Entity entity = new;
                entity.setText(untaint result);
                response.setEntity(entity);
            } else if (result is error) {
                setErrorResponse(response, result);
            }
        }

        _ = caller -> respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/jsonbodypart"
    }
    multipart2 (endpoint caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            var result = bodyParts[0].getJson();
            if (result is json) {
                response.setJsonPayload(untaint result);
            } else if (result is error) {
                setErrorResponse(response, result);
            }
        }
        _ = caller -> respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/xmlbodypart"
    }
    multipart3 (endpoint caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            var result = bodyParts[0].getXml();
            if (result is xml) {
                response.setXmlPayload(untaint result);
            } else if (result is error) {
                setErrorResponse(response, result);
            }
        }
         _ = caller -> respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/binarybodypart"
    }
    multipart4 (endpoint caller, http:Request request) {
        http:Response response = new;
        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            var result = bodyParts[0].getByteArray();
            if (result is byte[]) {
                response.setBinaryPayload(untaint result);
            } else if (result is error) {
                setErrorResponse(response, result);
            }
        }
        _ = caller -> respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/multipleparts"
    }
    multipart5 (endpoint caller, http:Request request) {
        http:Response response = new;
        //match request.getBodyParts() {
        //    error err => {
        //        setErrorResponse(response, err);
        //    }
        //    mime:Entity[] bodyParts => {
        //        string content = "";
        //        int i = 0;
        //        while (i < lengthof bodyParts) {
        //            mime:Entity part = bodyParts[i];
        //            content = content + " -- " + handleContent(part);
        //            i = i + 1;
        //        }
        //        response.setTextPayload(untaint content);
        //    }
        //}

        var bodyParts = request.getBodyParts();

        if (bodyParts is mime:Entity[]) {
            string content = "";
            int i = 0;
            while (i < lengthof bodyParts) {
                mime:Entity part = bodyParts[i];
                content = content + " -- " + handleContent(part);
                i = i + 1;
            }
            response.setTextPayload(untaint content);
        }
        _ = caller -> respond(untaint response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/emptyparts"
    }
    multipart6 (endpoint caller, http:Request request) {
        http:Response response = new;
        match (request.getBodyParts()) {
            error err => {
                response.setPayload(untaint <string>err.detail().message);
            }
            mime:Entity[] entity => {
                response.setPayload("Body parts detected!");
            }
        }
        _ = caller -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/nestedparts"
    }
    multipart7 (endpoint caller, http:Request request) {
        http:Response response = new;
        match request.getBodyParts() {
            error err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
                string payload = "";
                int i = 0;
                while (i < lengthof bodyParts) {
                    mime:Entity part = bodyParts[i];
                    payload = handleNestedParts(part);
                    i = i + 1;
                }
                response.setTextPayload(untaint payload);
            }
        }
        _ = caller -> respond(untaint response);
    }
}

function handleNestedParts (mime:Entity parentPart) returns (string) {
    string content = "";
    string contentTypeOfParent = parentPart.getContentType();
    if (contentTypeOfParent.hasPrefix("multipart/")) {
        match parentPart.getBodyParts() {
            error err => {
                return "Error decoding nested parts";
            }
            mime:Entity[] childParts => {
            int i = 0;
                while (i < lengthof childParts) {
                    mime:Entity childPart = childParts[i];
                    content = content + handleContent(childPart);
                    i = i + 1;
                }
            }
        }
    }
    return content;
}

function handleContent (mime:Entity bodyPart) returns (string) {
    mime:MediaType mediaType = check mime:getMediaType(bodyPart.getContentType());
    string baseType = mediaType.getBaseType();
    if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
        var payload = bodyPart.getXml();
        match payload {
            error err => return "Error in getting xml payload";
            xml xmlContent => return xmlContent.getTextValue();
        }
    } else if (mime:APPLICATION_JSON == baseType) {
        var payload = bodyPart.getJson();
        match payload {
            error err => return "Error in getting json payload";
            json jsonContent => {
               return extractFieldValue(jsonContent.bodyPart);
            }
        }
    } else if (mime:TEXT_PLAIN == baseType) {
        var payload = bodyPart.getText();
        match payload {
            error err => return "Error in getting string payload";
            string textContent => return textContent;
        }
    } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
        var payload = bodyPart.getByteArray();
        match payload {
            error err => return "Error in getting byte[] payload";
            byte[] blobContent => return mime:byteArrayToString(blobContent, mime:DEFAULT_CHARSET);
      }
    }
    return "";
}

//Keep this until there's a simpler way to get a string value out of a json
function extractFieldValue(json fieldValue) returns string {
     match fieldValue {
        int i => return "error";
        string s => return s;
        boolean b => return "error";
        json j => return "error";
    }
}

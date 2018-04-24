import ballerina/http;
import ballerina/io;
import ballerina/mime;

function setErrorResponse(http:Response response,  error err) {
    response.statusCode = 500;
    response.setTextPayload(err.message);
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
        match request.getBodyParts() {
            error err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
                match bodyParts[0].getText() {
                    error err => {
                         setErrorResponse(response, err);
                    }
                    string textPayload => {
                            mime:Entity entity = new;
                            entity.setText(textPayload);
                            response.setEntity(entity);
                    }
                }
            }
        }
        _ = caller -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/jsonbodypart"
    }
    multipart2 (endpoint caller, http:Request request) {
        http:Response response = new;
        match request.getBodyParts() {
            error err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
                match bodyParts[0].getJson() {
                    error err => {
                        setErrorResponse(response, err);
                    }
                    json jsonContent => {response.setJsonPayload(jsonContent);}
                }
            }
        }
        _ = caller -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/xmlbodypart"
    }
    multipart3 (endpoint caller, http:Request request) {
        http:Response response = new;
        match request.getBodyParts() {
            error err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
               match bodyParts[0].getXml() {
                    xml xmlContent => {response.setXmlPayload(xmlContent);}
                    error err => {
                        setErrorResponse(response, err);
                    }
               }
            }
         }
         _ = caller -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/binarybodypart"
    }
    multipart4 (endpoint caller, http:Request request) {
        http:Response response = new;
        match request.getBodyParts() {
            error err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
            match bodyParts[0].getBlob() {
                  blob blobContent => {response.setBinaryPayload(blobContent);}
                  error err => {
                        setErrorResponse(response, err);
                  }
                }
            }
        }
        _ = caller -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/multipleparts"
    }
    multipart5 (endpoint caller, http:Request request) {
        http:Response response = new;
        match request.getBodyParts() {
            error err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
                string content = "";
                int i = 0;
                while (i < lengthof bodyParts) {
                    mime:Entity part = bodyParts[i];
                    content = content + " -- " + handleContent(part);
                    i = i + 1;
                }
                response.setTextPayload(content);
            }
        }
        _ = caller -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/emptyparts"
    }
    multipart6 (endpoint caller, http:Request request) {
        http:Response response = new;
        match (request.getBodyParts()) {
            error err => {
                response.setTextPayload(err.message);
            }
            mime:Entity[] entity => {
                response.setTextPayload("Body parts detected!");
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
                response.setTextPayload(payload);
            }
        }
        _ = caller -> respond(response);
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
        var payload = bodyPart.getBlob();
        match payload {
            error err => return "Error in getting blob payload";
            blob blobContent => return blobContent.toString(mime:DEFAULT_CHARSET);
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

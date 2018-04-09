import ballerina/http;
import ballerina/io;
import ballerina/mime;

function setErrorResponse(http:Response response,  mime:EntityError err) {
    response.statusCode = 500;
    response.setStringPayload(err.message);
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
    multipart1 (endpoint client, http:Request request) {
        http:Response response = new;
        match request.getMultiparts() {
            mime:EntityError err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
                match bodyParts[0].getText() {
                    mime:EntityError err => {
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
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/jsonbodypart"
    }
    multipart2 (endpoint client, http:Request request) {
        http:Response response = new;
        match request.getMultiparts() {
            mime:EntityError err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
                match bodyParts[0].getJson() {
                    mime:EntityError err => {
                        setErrorResponse(response, err);
                    }
                    json jsonContent => {response.setJsonPayload(jsonContent);}
                }
            }
        }
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/xmlbodypart"
    }
    multipart3 (endpoint client, http:Request request) {
        http:Response response = new;
        match request.getMultiparts() {
            mime:EntityError err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
               match bodyParts[0].getXml() {
                    xml xmlContent => {response.setXmlPayload(xmlContent);}
                    mime:EntityError err => {
                        setErrorResponse(response, err);
                    }
               }
            }
         }
         _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/binarybodypart"
    }
    multipart4 (endpoint client, http:Request request) {
        http:Response response = new;
        match request.getMultiparts() {
            mime:EntityError err => {
                setErrorResponse(response, err);
            }
            mime:Entity[] bodyParts => {
            match bodyParts[0].getBlob() {
                  blob blobContent => {response.setBinaryPayload(blobContent);}
                  mime:EntityError err => {
                        setErrorResponse(response, err);
                  }
                }
            }
        }
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/multipleparts"
    }
    multipart5 (endpoint client, http:Request request) {
        http:Response response = new;
        match request.getMultiparts() {
            mime:EntityError err => {
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
                response.setStringPayload(content);
            }
        }
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/emptyparts"
    }
    multipart6 (endpoint client, http:Request request) {
        http:Response response = new;
        match (request.getMultiparts()) {
            mime:EntityError err => {
                response.setStringPayload(err.message);
            }
            mime:Entity[] entity => {
                response.setStringPayload("Body parts detected!");
            }
        }
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/nestedparts"
    }
    multipart7 (endpoint client, http:Request request) {
        http:Response response = new;
        match request.getMultiparts() {
            mime:EntityError err => {
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
                response.setStringPayload(payload);
            }
        }
        _ = client -> respond(response);
    }
}

function handleNestedParts (mime:Entity parentPart) returns (string) {
    string content = "";
    string contentTypeOfParent = parentPart.contentType.toString();
    if (contentTypeOfParent.hasPrefix("multipart/")) {
        match parentPart.getBodyParts() {
            mime:EntityError err => {
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
    string contentType = bodyPart.contentType.toString();
    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
        var payload = bodyPart.getXml();
        match payload {
            mime:EntityError err => return "Error in getting xml payload";
            xml xmlContent => return xmlContent.getTextValue();
        }
    } else if (mime:APPLICATION_JSON == contentType) {
        var payload = bodyPart.getJson();
        match payload {
            mime:EntityError err => return "Error in getting json payload";
            json jsonContent => {
               return extractFieldValue(jsonContent.bodyPart);
            }
        }
    } else if (mime:TEXT_PLAIN == contentType) {
        var payload = bodyPart.getText();
        match payload {
            mime:EntityError err => return "Error in getting string payload";
            string textContent => return textContent;
        }
    } else if (mime:APPLICATION_OCTET_STREAM == contentType) {
        var payload = bodyPart.getBlob();
        match payload {
            mime:EntityError err => return "Error in getting blob payload";
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

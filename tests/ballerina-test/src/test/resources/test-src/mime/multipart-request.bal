import ballerina/net.http;
import ballerina/net.http.mock;

import ballerina/mime;

function setErrorResponse(http:Response response,  mime:EntityError err) {
    response.statusCode = 500;
    response.setStringPayload(err.message);
}

endpoint http:ServiceEndpoint mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/test"}
service<http:Service> test bind mockEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/textbodypart"
    }
    multipart1 (endpoint client, http:Request request) {
        http:Response response = {};
        match request.getMultiparts() {
            mime:Entity[] bodyParts => {
                match bodyParts[0].getText() {
                    string textPayload => {
                            io:println("------------");
                            io:println(textPayload);
                            io:println("------------");
                            mime:Entity enti = {};
                            enti.setText(textPayload);
                            response.setEntity(enti);
                    }
                    mime:EntityError err => {
                         io:println("---Error---------");
                         setErrorResponse(response, err);
                    }
                    int | null => {
                        io:println("---any or null---------");
                        response.setStringPayload("Text payload is null");
                    }
                }
            }
            mime:EntityError err => {
                io:println("--outer error--------");
                setErrorResponse(response, err);
            }
        }
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/jsonbodypart"
    }
    multipart2 (endpoint client, http:Request request) {
        http:Response response = {};
        match request.getMultiparts() {
            mime:Entity[] bodyParts => {
                match bodyParts[0].getJson() {
                    json jsonContent => {response.setJsonPayload(jsonContent);}
                    mime:EntityError err => {
                        setErrorResponse(response, err);
                    }
                }
            }
            mime:EntityError err => {
                setErrorResponse(response, err);
            }
        }
        _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/xmlbodypart"
    }
    multipart3 (endpoint client, http:Request request) {
        http:Response response = {};
        match request.getMultiparts() {
              mime:Entity[] bodyParts => {
                   match bodyParts[0].getXml() {
                        xml xmlContent => {response.setXmlPayload(xmlContent);}
                        mime:EntityError err => {
                            setErrorResponse(response, err);
                        }
                   }
              }
              mime:EntityError err => {
                    setErrorResponse(response, err);
              }
         }
         _ = client -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/binarybodypart"
    }
    multipart4 (endpoint client, http:Request request) {
        http:Response response = {};
        match request.getMultiparts() {
              mime:Entity[] bodyParts => {
                match bodyParts[0].getBlob() {
                      blob blobContent => {response.setBinaryPayload(blobContent);}
                      mime:EntityError err => {
                            setErrorResponse(response, err);
                      }
                }
              }
              mime:EntityError err => {
                    setErrorResponse(response, err);
              }
        }
        _ = client -> respond(response);
    }

    //@http:ResourceConfig {
    //    methods:["POST"],
    //    path:"/multipleparts"
    //}
    //multipart5 (endpoint client, http:Request request) {
    //    //var bodyParts, _ = request.getMultiparts();
    //    //int i = 0;
    //    //string content = "";
    //    //while (i < lengthof bodyParts) {
    //    //    mime:Entity part = bodyParts[i];
    //    //    content = content + " -- " + handleContent(part);
    //    //    i = i + 1;
    //    //}
    //    //http:Response response = {};
    //    //response.setStringPayload(content);
    //    //_ = conn -> respond(response);
    //    http:Response response = {};
    //    match request.getMultiparts() {
    //
    //    }

    //@http:ResourceConfig {
    //    methods:["POST"],
    //    path:"/emptyparts"
    //}
    //multipart6 (endpoint client, http:Request request) {
    //    var entity, entityError = request.getMultiparts();
    //    http:Response response = {};
    //    response.setStringPayload(entityError.message);
    //    _ = conn -> respond(response);
    //}
    //
    //@http:ResourceConfig {
    //    methods:["POST"],
    //    path:"/nestedparts"
    //}
    //multipart7 (endpoint client, http:Request request) {
    //    var parentParts, _ = request.getMultiparts();
    //    int i = 0;
    //    string content = "";
    //    while (i < lengthof parentParts) {
    //        mime:Entity parentPart = parentParts[i];
    //        content = handleNestedParts(parentPart);
    //        i = i + 1;
    //    }
    //    http:Response response = {};
    //    response.setStringPayload(content);
    //    _ = conn -> respond(response);
    //}
}

//function handleNestedParts (mime:Entity parentPart) (string) {
//    var childParts, _ = parentPart.getBodyParts();
//    int i = 0;
//    string content = "";
//    if (childParts != null) {
//        while (i < lengthof childParts) {
//            mime:Entity childPart = childParts[i];
//            content = content + handleContent(childPart);
//            i = i + 1;
//        }
//    }
//    return content;
//}
//
//function handleContent (mime:Entity bodyPart) (string) {
//    string contentType = bodyPart.contentType.toString();
//    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
//        var xmlContent, _ = bodyPart.getXml();
//        return xmlContent.getTextValue();
//    } else if (mime:APPLICATION_JSON == contentType) {
//        var jsonContent, _ = bodyPart.getJson();
//        var jsonValue, _ = (string)jsonContent.bodyPart;
//        return jsonValue;
//    } else if (mime:TEXT_PLAIN == contentType) {
//        var textData, _ = bodyPart.getText();
//        return textData;
//    } else if (mime:APPLICATION_OCTET_STREAM == contentType) {
//        var blobContent, _ = bodyPart.getBlob();
//        return blobContent.toString(mime:DEFAULT_CHARSET);
//    }
//    return null;
//}

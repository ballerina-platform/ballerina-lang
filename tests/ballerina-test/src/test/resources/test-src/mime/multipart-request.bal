import ballerina/net.http;
import ballerina/net.http.mock;

import ballerina/mime;

endpoint<mock:NonListeningService> mockEP {
    port:9090
}

@http:serviceConfig {endpoints:[mockEP]}
service<http:Service> test {

    @http:resourceConfig {
        methods:["POST"],
        path:"/textbodypart"
    }
    resource multipart1 (http:ServerConnector conn, http:Request request) {
        var bodyParts, _ = request.getMultiparts();
        var textContent, _ = bodyParts[0].getText();
        http:Response response = {};
        response.setStringPayload(textContent);
        _ = conn -> respond(response);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/jsonbodypart"
    }
    resource multipart2 (http:ServerConnector conn, http:Request request) {
        var bodyParts, _ = request.getMultiparts();
        var jsonContent, _ = bodyParts[0].getJson();
        http:Response response = {};
        response.setJsonPayload(jsonContent);
        _ = conn -> respond(response);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/xmlbodypart"
    }
    resource multipart3 (http:ServerConnector conn, http:Request request) {
        var bodyParts, _ = request.getMultiparts();
        var xmlContent, _ = bodyParts[0].getXml();
        http:Response response = {};
        response.setXmlPayload(xmlContent);
        _ = conn -> respond(response);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/binarybodypart"
    }
    resource multipart4 (http:ServerConnector conn, http:Request request) {
        var bodyParts, _ = request.getMultiparts();
        var blobContent, _ = bodyParts[0].getBlob();
        http:Response response = {};
        response.setBinaryPayload(blobContent);
        _ = conn -> respond(response);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/multipleparts"
    }
    resource multipart5 (http:ServerConnector conn, http:Request request) {
        var bodyParts, _ = request.getMultiparts();
        int i = 0;
        string content = "";
        while (i < lengthof bodyParts) {
            mime:Entity part = bodyParts[i];
            content = content + " -- " + handleContent(part);
            i = i + 1;
        }
        http:Response response = {};
        response.setStringPayload(content);
        _ = conn -> respond(response);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/emptyparts"
    }
    resource multipart6 (http:ServerConnector conn, http:Request request) {
        var entity, entityError = request.getMultiparts();
        http:Response response = {};
        response.setStringPayload(entityError.message);
        _ = conn -> respond(response);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/nestedparts"
    }
    resource multipart7 (http:ServerConnector conn, http:Request request) {
        var parentParts, _ = request.getMultiparts();
        int i = 0;
        string content = "";
        while (i < lengthof parentParts) {
            mime:Entity parentPart = parentParts[i];
            content = handleNestedParts(parentPart);
            i = i + 1;
        }
        http:Response response = {};
        response.setStringPayload(content);
        _ = conn -> respond(response);
    }
}

function handleNestedParts (mime:Entity parentPart) (string) {
    var childParts, _ = parentPart.getBodyParts();
    int i = 0;
    string content = "";
    if (childParts != null) {
        while (i < lengthof childParts) {
            mime:Entity childPart = childParts[i];
            content = content + handleContent(childPart);
            i = i + 1;
        }
    }
    return content;
}

function handleContent (mime:Entity bodyPart) (string) {
    string contentType = bodyPart.contentType.toString();
    if (mime:APPLICATION_XML == contentType || mime:TEXT_XML == contentType) {
        var xmlContent, _ = bodyPart.getXml();
        return xmlContent.getTextValue();
    } else if (mime:APPLICATION_JSON == contentType) {
        var jsonContent, _ = bodyPart.getJson();
        var jsonValue, _ = (string)jsonContent.bodyPart;
        return jsonValue;
    } else if (mime:TEXT_PLAIN == contentType) {
        var textData, _ = bodyPart.getText();
        return textData;
    } else if (mime:APPLICATION_OCTET_STREAM == contentType) {
        var blobContent, _ = bodyPart.getBlob();
        return blobContent.toString(mime:DEFAULT_CHARSET);
    }
    return null;
}

package ballerina.net.http;

import ballerina.mime;
import ballerina.file;

const string HEADER_VAL_100_CONTINUE = "100-continue";
const string HEADER_KEY_EXPECT = "Expect";

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"req: A inbound request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value for the provided header name. Returns null if the header does not exist."}
documentation {
Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned.
- #req A inbound request message.
- #headerName The header name.
}
public function <InRequest req> getHeader (string headerName) (string) {
    mime:Entity entity = req.getEntityWithoutBody();
    return getFirstHeaderFromEntity(entity, headerName);
}

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"req: A outbound request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value for the provided header name. Returns null if the header does not exist."}
documentation {
Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned.
- #req A outbound request message.
- #headerName The header name.
}
public function <OutRequest req> getHeader (string headerName) (string) {
    mime:Entity entity = req.getEntityWithoutBody();
    return getFirstHeaderFromEntity(entity, headerName);
}

@Description {value:"Adds the specified key/value pair as an HTTP header to the outbound request"}
@Param {value:"req: A outbound request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
documentation {
Adds the specified key/value pair as an HTTP header to the outbound request.
- #req A outbound request message.
- #headerName The header name.
- #headerValue The header value.
}
public function <OutRequest req> addHeader (string headerName, string headerValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    addHeaderToEntity(entity, headerName, headerValue);
}

@Description {value:"Gets transport headers from the inbound request"}
@Param {value:"req: A inbound request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
documentation {
Gets transport headers from the inbound request.
- #req A inbound request message.
- #headerName The header name.
}
public function <InRequest req> getHeaders (string headerName) (string[]) {
    mime:Entity entity = req.getEntityWithoutBody();
    return getHeadersFromEntity(entity, headerName);
}

@Description {value:"Gets transport headers from the outbound request"}
@Param {value:"req: A outbound request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
documentation {
Gets transport headers from the outbound request.
- #req A outbound request message.
- #headerName The header name.
}
public function <OutRequest req> getHeaders (string headerName) (string[]) {
    mime:Entity entity = req.getEntityWithoutBody();
    return getHeadersFromEntity(entity, headerName);
}

@Description {value:"Sets the value of a transport header"}
@Param {value:"req: A outbound request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
documentation {
Sets the value of a transport header.
- #req A outbound request message.
- #headerName The header name.
- #headerValue The header value.
}
public function <OutRequest req> setHeader (string headerName, string headerValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    setHeaderToEntity(entity, headerName, headerValue);
}

@Description {value:"Removes a transport header from the outbound request"}
@Param {value:"req: A outbound request message"}
@Param {value:"key: The header name"}
documentation {
Removes a transport header from the outbound request.
- #req A outbound request message.
- #key The header name.
}
public function <OutRequest req> removeHeader (string key) {
    mime:Entity entity = req.getEntityWithoutBody();
    entity.headers.remove(key);
}

@Description {value:"Removes all transport headers from the message"}
@Param {value:"req: A outbound request message"}
documentation {
Removes all transport headers from the message.
- #req A outbound request message.
}
public function <OutRequest req> removeAllHeaders () {
    mime:Entity entity = req.getEntityWithoutBody();
    entity.headers = {};
}

@Description {value:"Checks whether the client expects a 100-continue response."}
@Param {value:"req: A inbound request message"}
@Return {value:"Returns true if the client expects a 100-continue response. If not, returns false."}
documentation {
Checks whether the client expects a 100-continue response.
- #req A inbound request message.
}
public function <InRequest req> expects100Continue () (boolean) {
    var expectHeader = req.getHeader(HEADER_KEY_EXPECT);
    if (expectHeader != null && expectHeader == HEADER_VAL_100_CONTINUE) {
        return true;
    }
    return false;
}

@Description {value:"Gets the Content-Length header from the inbound request"}
@Param {value:"req: A inbound request message"}
@Return {value:"length of the message"}
documentation {
Gets the Content-Length header from the inbound request.
- #request A inbound request message.
}
public function <InRequest request> getContentLength () (int) {
    if (request.getHeader(CONTENT_LENGTH) != null) {
        string strContentLength = request.getHeader(CONTENT_LENGTH);
        return getContentLengthIntValue(strContentLength);
    }
    return -1;
}

@Description {value:"Gets the Content-Length header from the Outbound request"}
@Param {value:"req: A Outbound request message"}
@Return {value:"length of the message"}
documentation {
Gets the Content-Length header from the Outbound request.
- #request A Outbound request message.
}
public function <OutRequest request> getContentLength () (int) {
    if (request.getHeader(CONTENT_LENGTH) != null) {
        string strContentLength = request.getHeader(CONTENT_LENGTH);
        return getContentLengthIntValue(strContentLength);
    }
    return -1;
}

@Description {value:"Gets the inbound request payload in JSON format"}
@Param {value:"request: The inbound request message"}
@Return {value:"The JSON reresentation of the message payload"}
documentation {
Gets the inbound request payload in JSON format.
- #request The inbound request message.
}
public function <InRequest request> getJsonPayload () (json) {
    mime:Entity entity = request.getEntity();
    return mime:getJson(entity);
}

@Description {value:"Gets the outbound request payload in JSON format"}
@Param {value:"request: The outbound request message"}
@Return {value:"The JSON reresentation of the message payload"}
documentation {
Gets the outbound request payload in JSON format.
- #request The outbound request message.
}
public function <OutRequest request> getJsonPayload () (json) {
    mime:Entity entity = request.getEntity();
    return mime:getJson(entity);
}

@Description {value:"Gets the inbound request payload in XML format"}
@Param {value:"request: The inbound request message"}
@Return {value:"The XML representation of the message payload"}
documentation {
Gets the inbound request payload in XML format.
- #request The inbound request message.
}
public function <InRequest request> getXmlPayload () (xml) {
    mime:Entity entity = request.getEntity();
    return mime:getXml(entity);
}

@Description {value:"Gets the outbound request payload in XML format"}
@Param {value:"request: The outbound request message"}
@Return {value:"The XML representation of the message payload"}
documentation {
Gets the outbound request payload in XML format.
- #request The outbound request message.
}
public function <OutRequest request> getXmlPayload () (xml) {
    mime:Entity entity = request.getEntity();
    return mime:getXml(entity);
}

@Description {value:"Gets the inbound request payload as a string"}
@Param {value:"request: inbound request message"}
@Return {value:"The string representation of the message payload"}
documentation {
Gets the inbound request payload as a string.
- #request inbound request message.
}
public function <InRequest request> getStringPayload () (string) {
    mime:Entity entity = request.getEntity();
    return mime:getText(entity);
}

@Description {value:"Gets the outbound request payload as a string"}
@Param {value:"request: outbound request message"}
@Return {value:"The string representation of the message payload"}
documentation {
Gets the outbound request payload as a string.
- #request outbound request message.
}
public function <OutRequest request> getStringPayload () (string) {
    mime:Entity entity = request.getEntity();
    return mime:getText(entity);
}

@Description {value:"Gets the inboundrequest payload in blob format"}
@Param {value:"request: The inbound request message"}
@Return {value:"The blob representation of the message payload"}
documentation {
Gets the inbound request payload in blob format.
- #request The inbound request message.
}
public function <InRequest request> getBinaryPayload () (blob) {
    mime:Entity entity = request.getEntity();
    return mime:getBlob(entity);
}

@Description {value:"Gets the outbound request payload in blob format"}
@Param {value:"request: The outbound request message"}
@Return {value:"The blob representation of the message payload"}
documentation {
Gets the outbound request payload in blob format.
- #request The outbound request message.
}
public function <OutRequest request> getBinaryPayload () (blob) {
    mime:Entity entity = request.getEntity();
    return mime:getBlob(entity);
}

@Description {value:"Gets the form parameters from the HTTP request as a map"}
@Param {value:"req: The inbound request message"}
@Return {value:"The map of form params"}
documentation {
Gets the form parameters from the HTTP request as a map.
- #request The inbound request message.
}
public function <InRequest request> getFormParams () (map) {
    mime:Entity entity = request.getEntity();
    string formData = mime:getText(entity);
    map parameters = {};
    if (formData != null && formData != "") {
        string[] entries = formData.split("&");
        int entryIndex = 0;
        while (entryIndex < lengthof entries) {
            int index = entries[entryIndex].indexOf("=");
            if (index != -1) {
                string name = entries[entryIndex].subString(0, index).trim();
                int size = entries[entryIndex].length();
                string value = entries[entryIndex].subString(index + 1, size).trim();
                if (value != "") {
                    parameters[name] = value;
                }
            }
            entryIndex = entryIndex + 1;
        }
    }
    return parameters;
}

@Description {value:"Get multiparts from inbound request"}
@Param {value:"req: The request message"}
@Return {value:"Returns the body parts as an array of entities"}
documentation {
Get multiparts from inbound request.
- #request The request message.
}
public function <InRequest request> getMultiparts () (mime:Entity[]) {
    mime:Entity entity = request.getEntity();
    return entity.multipartData;
}

@Description {value:"Get multiparts from outbound request"}
@Param {value:"req: The request message"}
@Return {value:"Returns the body parts as an array of entities"}
documentation {
Get multiparts from outbound request.
- #request The request message.
}
public function <OutRequest request> getMultiparts () (mime:Entity[]) {
    mime:Entity entity = request.getEntity();
    return entity.multipartData;
}

@Description {value:"Sets a JSON as the outbound request payload"}
@Param {value:"request: The outbound request message"}
@Param {value:"payload: The JSON payload to be set to the request"}
documentation {
Sets a JSON as the outbound request payload.
- #request The outbound request message.
- #payload The JSON payload to be set to the request.
}
public function <OutRequest request> setJsonPayload (json payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.jsonData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_JSON);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Sets an XML as the payload"}
@Param {value:"request: The outbound request message"}
@Param {value:"payload: The XML payload object"}
documentation {
Sets an XML as the payload.
- #request The outbound request message.
- #payload The XML payload object.
}
public function <OutRequest request> setXmlPayload (xml payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.xmlData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_XML);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Sets a string as the outbound request payload"}
@Param {value:"request: The outbound request message"}
@Param {value:"payload: The payload to be set to the request as a string"}
documentation {
Sets a string as the outbound request payload.
- #request The outbound request message.
- #payload The payload to be set to the request as a string.
}
public function <OutRequest request> setStringPayload (string payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.textData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:TEXT_PLAIN);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Sets a blob as the outbound request payload"}
@Param {value:"request: outbound request message"}
@Param {value:"payload: The blob representation of the message payload"}
documentation {
Sets a blob as the outbound request payload.
- #request Outbound request message.
- #payload The blob representation of the message payload.
}
public function <OutRequest request> setBinaryPayload (blob payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.byteData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Set multiparts as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"bodyParts: Represent body parts that needs to be set to the request"}
documentation {
Set multiparts as the request payload.
- #request The request message.
- #bodyParts Represent body parts that needs to be set to the request.
}
public function <OutRequest request> setMultiparts (mime:Entity[] bodyParts) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.multipartData = bodyParts;
    mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_FORM_DATA);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Sets the entity body of the outbound request with the given file content"}
@Param {value:"request: The outbound request message"}
@Param {value:"content: File containing the actual content"}
@Param {value:"contentType: Content-Type of the given data"}
documentation {
Sets the entity body of the outbound request with the given file content.
- #request The request message.
- #content File containing the actual content.
- #contentType Content-Type of the given data.
}
public function <OutRequest request> setEntityBody (file:File content, string contentType) {
    mime:MediaType mediaType = mime:getMediaType(contentType);
    mime:Entity entity = request.getEntityWithoutBody();
    entity.contentType = mediaType;
    entity.overflowData = content;
    request.setEntity(entity);
}

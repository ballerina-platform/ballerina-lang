package ballerina.net.http;

import ballerina.mime;
import ballerina.file;

@Description {value:"Gets the named HTTP header from the inbound response"}
@Param {value:"res: The inbound response struct"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value struct for the provided header name. Returns null if the header does not exist."}
public function <InResponse res> getHeader (string headerName) (mime:HeaderValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    mime:HeaderValue[] headerValues = getHeadersFromEntity(entity, headerName);
    return headerValues != null ? headerValues[0] : null;
}

@Description {value:"Gets the named HTTP header from the outbound response"}
@Param {value:"res: The outbound response struct"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value struct for the provided header name. Returns null if the header does not exist."}
public function <OutResponse res> getHeader (string headerName) (mime:HeaderValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    mime:HeaderValue[] headerValues = getHeadersFromEntity(entity, headerName);
    return headerValues != null ? headerValues[0] : null;
}

@Description {value:"Adds the specified key/value pair as an HTTP header to the outbound response"}
@Param {value:"res: The outbound response message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <OutResponse res> addHeader (string headerName, string headerValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    addHeaderToEntity(entity, headerName, headerValue);
}

@Description {value:"Gets the HTTP headers from the inbound response"}
@Param {value:"res: The inbound response message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <InResponse res> getHeaders (string headerName) (mime:HeaderValue[]) {
    mime:Entity entity = res.getEntityWithoutBody();
    return getHeadersFromEntity(entity, headerName);
}

@Description {value:"Gets the HTTP headers from the outbound response"}
@Param {value:"res: The outbound response message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <OutResponse res> getHeaders (string headerName) (mime:HeaderValue[]) {
    mime:Entity entity = res.getEntityWithoutBody();
    return getHeadersFromEntity(entity, headerName);
}

@Description {value:"Sets the value of a transport header"}
@Param {value:"res: The outbound response message"}
@Param {value:"key: The header name"}
@Param {value:"value: The header value"}
public function <OutResponse res> setHeader (string key, string value) {
    mime:Entity entity = res.getEntityWithoutBody();
    if (entity.headers == null) {
        entity.headers = {};
    }
    mime:HeaderValue[] header = [{value:value}];
    entity.headers[key] = header;
}

@Description {value:"Sets the value of a transport header with multiple header values"}
@Param {value:"res: The outbound response message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValues: An array of header values"}
public function <OutResponse res> setHeaders (string headerName, mime:HeaderValue[] headerValues) {
    mime:Entity entity = res.getEntityWithoutBody();
    if (entity.headers == null) {
        entity.headers = {};
    }
    entity.headers[headerName] = headerValues;
}

@Description {value:"Removes a transport header from the response"}
@Param {value:"res: The response message"}
@Param {value:"key: The header name"}
public function <OutResponse res> removeHeader (string key) {
    mime:Entity entity = res.getEntityWithoutBody();
    if (entity.headers == null) {
        return;
    }
    entity.headers.remove(key);
}

@Description {value:"Removes all transport headers from the response"}
@Param {value:"res: The outbound response message"}
public function <OutResponse res> removeAllHeaders () {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.headers = {};
}

@Description {value:"Gets the Content-Length header value from the inbound response"}
@Param {value:"response: The inbound response message"}
@Return {value:"length of the message"}
public function <InResponse response> getContentLength () (int) {
    if (response.getHeader(CONTENT_LENGTH) != null) {
        string strContentLength = response.getHeader(CONTENT_LENGTH).value;
        return getContentLengthIntValue(strContentLength);
    }
    return -1;
}

@Description {value:"Gets the inbound response payload in JSON format"}
@Param {value:"response: The inbound response message"}
@Return {value:"The JSON reresentation of the message payload"}
public function <InResponse response> getJsonPayload () (json) {
    mime:Entity entity = response.getEntity();
    return mime:getJson(entity);
}

@Description {value:"Gets the inbound response payload in XML format"}
@Param {value:"response: The inbound response message"}
@Return {value:"The XML representation of the message payload"}
public function <InResponse response> getXmlPayload () (xml) {
    mime:Entity entity = response.getEntity();
    return mime:getXml(entity);
}

@Description {value:"Gets the inbound response payload as a string"}
@Param {value:"response: The inbound response message"}
@Return {value:"The string representation of the message payload"}
public function <InResponse response> getStringPayload () (string) {
    mime:Entity entity = response.getEntity();
    return mime:getText(entity);
}

@Description {value:"Gets the inbound response payload in blob format"}
@Param {value:"response: The inbound response message"}
@Return {value:"The blob representation of the message payload"}
public function <InResponse response> getBinaryPayload () (blob) {
    mime:Entity entity = response.getEntity();
    return mime:getBlob(entity);
}

@Description {value:"Sets a JSON as the outbound response payload"}
@Param {value:"response: The outbound response message"}
@Param {value:"payload: The JSON payload object"}
public function <OutResponse response> setJsonPayload (json payload) {
    mime:Entity entity = {};
    entity.jsonData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_JSON);
    entity.contentType = mediaType;
    entity.isInMemory = true;
    response.setEntity(entity);
}

@Description {value:"Sets an XML as the outbound response payload"}
@Param {value:"response: The outbound response message"}
@Param {value:"payload: The XML payload object"}
public function <OutResponse response> setXmlPayload (xml payload) {
    mime:Entity entity = {};
    entity.xmlData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_XML);
    entity.contentType = mediaType;
    entity.isInMemory = true;
    response.setEntity(entity);
}

@Description { value:"Sets a string as the outbound response payload"}
@Param { value:"response: The outbound response message" }
@Param { value:"payload: The payload to be set to the response as a string" }
public function <OutResponse response> setStringPayload (string payload) {
    mime:Entity entity = {};
    entity.textData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:TEXT_PLAIN);
    entity.contentType = mediaType;
    entity.isInMemory = true;
    response.setEntity(entity);
}

@Description {value:"Sets a blob as the outbound response payload"}
@Param {value:"response: The outbound response message"}
@Param {value:"payload: The blob representation of the message payload"}
public function <OutResponse response> setBinaryPayload (blob payload) {
    mime:Entity entity = {};
    entity.byteData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    entity.contentType = mediaType;
    entity.isInMemory = true;
    response.setEntity(entity);
}

@Description {value:"Sets the entity body of the outbound response with the given file content"}
@Param {value:"response: The outbound response message"}
@Param {value:"content: File containing the actual content"}
@Param {value:"contentType: Content-Type of the given data"}
public function <OutResponse response> setEntityBody(file:File content, string contentType) {
    mime:MediaType mediaType = mime:getMediaType(contentType);
    mime:Entity entity = response.getEntityWithoutBody();
    entity.contentType = mediaType;
    entity.isInMemory = false;
    entity.overflowData = content;
    response.setEntity(entity);
}

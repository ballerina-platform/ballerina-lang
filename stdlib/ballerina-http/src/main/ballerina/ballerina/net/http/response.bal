package ballerina.net.http;

import ballerina.file;
import ballerina.io;
import ballerina.mime;

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"res: The inbound response struct"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value struct for the provided header name. Returns null if the header does not exist."}
public function <InResponse res> getHeader (string headerName) (string) {
    mime:Entity entity = res.getEntityWithoutBody();
    return entity.getHeader(headerName);
}

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"res: The outbound response struct"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value struct for the provided header name. Returns null if the header does not exist."}
public function <OutResponse res> getHeader (string headerName) (string) {
    mime:Entity entity = res.getEntityWithoutBody();
    return entity.getHeader(headerName);
}

@Description {value:"Adds the specified key/value pair as an HTTP header to the outbound response"}
@Param {value:"res: The outbound response message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <OutResponse res> addHeader (string headerName, string headerValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.addHeader(headerName, headerValue);
}

@Description {value:"Gets the HTTP headers from the inbound response"}
@Param {value:"res: The inbound response message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <InResponse res> getHeaders (string headerName) (string[]) {
    mime:Entity entity = res.getEntityWithoutBody();
    return entity.getHeaders(headerName);
}

@Description {value:"Gets the HTTP headers from the outbound response"}
@Param {value:"res: The outbound response message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <OutResponse res> getHeaders (string headerName) (string[]) {
    mime:Entity entity = res.getEntityWithoutBody();
    return entity.getHeaders(headerName);
}

@Description {value:"Sets the value of a transport header"}
@Param {value:"res: The outbound response message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <OutResponse res> setHeader (string headerName, string headerValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.setHeader(headerName, headerValue);
}

@Description {value:"Removes a transport header from the response"}
@Param {value:"res: The response message"}
@Param {value:"key: The header name"}
public function <OutResponse res> removeHeader (string key) {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.removeHeader(key);
}

@Description {value:"Removes all transport headers from the response"}
@Param {value:"res: The outbound response message"}
public function <OutResponse res> removeAllHeaders () {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.removeAllHeaders();
}

@Description {value:"Gets the Content-Length header value from the inbound response"}
@Param {value:"response: The inbound response message"}
@Return {value:"length of the message"}
public function <InResponse response> getContentLength () (int) {
    mime:Entity entity = response.getEntityWithoutBody();
    return entity.getContentLength();
}

@Description {value:"Gets the Content-Length header value from the outbound response"}
@Param {value:"response: The outbound response message"}
@Return {value:"length of the message"}
public function <OutResponse response> getContentLength () (int) {
    mime:Entity entity = response.getEntityWithoutBody();
    return entity.getContentLength();
}

@Description {value:"Gets the inbound response payload in JSON format"}
@Param {value:"response: The inbound response message"}
@Return {value:"The JSON reresentation of the message payload"}
public function <InResponse response> getJsonPayload () (json, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getJson();
}

@Description {value:"Gets the outbound response payload in JSON format"}
@Param {value:"response: The outbound response message"}
@Return {value:"The JSON reresentation of the message payload"}
public function <OutResponse response> getJsonPayload () (json, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getJson();
}

@Description {value:"Gets the inbound response payload in XML format"}
@Param {value:"response: The inbound response message"}
@Return {value:"The XML representation of the message payload"}
public function <InResponse response> getXmlPayload () (xml, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getXml();
}

@Description {value:"Gets the outbound response payload in XML format"}
@Param {value:"response: The outbound response message"}
@Return {value:"The XML representation of the message payload"}
public function <OutResponse response> getXmlPayload () (xml, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getXml();
}

@Description {value:"Gets the inbound response payload as a string"}
@Param {value:"response: The inbound response message"}
@Return {value:"The string representation of the message payload"}
public function <InResponse response> getStringPayload () (string, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getText();
}

@Description {value:"Gets the outbound response payload as a string"}
@Param {value:"response: The outbound response message"}
@Return {value:"The string representation of the message payload"}
public function <OutResponse response> getStringPayload () (string, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getText();
}

@Description {value:"Gets the inbound response payload in blob format"}
@Param {value:"response: The inbound response message"}
@Return {value:"The blob representation of the message payload"}
public function <InResponse response> getBinaryPayload () (blob, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        blob byteData;
        return byteData, entityError;
    }
    return entity.getBlob();
}

@Description {value:"Gets the outbound response payload in blob format"}
@Param {value:"response: The outbound response message"}
@Return {value:"The blob representation of the message payload"}
public function <OutResponse response> getBinaryPayload () (blob, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        blob byteData;
        return byteData, entityError;
    }
    return entity.getBlob();
}

@Description {value:"Gets the inbound response payload as a byte channel except for multiparts. In case of multiparts,
please use 'getMultiparts()' instead."}
@Param {value:"response: The inbound response message"}
@Return {value:"A byte channel as the message payload"}
public function <InResponse response> getByteChannel () (io:ByteChannel, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getByteChannel();
}

@Description {value:"Gets the outbound response payload as a byte channel except for multiparts. In case of multiparts,
please use 'getMultiparts()' instead."}
@Param {value:"response: outbound response message"}
@Return {value:"A byte channel as the message payload"}
public function <OutResponse response> getByteChannel () (io:ByteChannel, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getByteChannel();
}

@Description {value:"Get multiparts from inbound response"}
@Param {value:"response: The response message"}
@Return {value:"Returns the body parts as an array of entities"}
public function <InResponse response> getMultiparts () (mime:Entity[], mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getBodyParts();
}

@Description {value:"Get multiparts from outbound response"}
@Param {value:"response: The response message"}
@Return {value:"Returns the body parts as an array of entities"}
public function <OutResponse response> getMultiparts () (mime:Entity[], mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getBodyParts();
}

@Description {value:"Sets a JSON as the outbound response payload"}
@Param {value:"response: The outbound response message"}
@Param {value:"payload: The JSON payload object"}
public function <OutResponse response> setJsonPayload (json payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setJson(payload);
    entity.contentType = getMediaTypeFromResponse(response, mime:APPLICATION_JSON);
    response.setEntity(entity);
}

@Description {value:"Sets an XML as the outbound response payload"}
@Param {value:"response: The outbound response message"}
@Param {value:"payload: The XML payload object"}
public function <OutResponse response> setXmlPayload (xml payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setXml(payload);
    entity.contentType = getMediaTypeFromResponse(response, mime:APPLICATION_XML);
    response.setEntity(entity);
}

@Description { value:"Sets a string as the outbound response payload"}
@Param { value:"response: The outbound response message" }
@Param { value:"payload: The payload to be set to the response as a string" }
public function <OutResponse response> setStringPayload (string payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setText(payload);
    entity.contentType = getMediaTypeFromResponse(response, mime:TEXT_PLAIN);
    response.setEntity(entity);
}

@Description {value:"Sets a blob as the outbound response payload"}
@Param {value:"response: The outbound response message"}
@Param {value:"payload: The blob representation of the message payload"}
public function <OutResponse response> setBinaryPayload (blob payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setBlob(payload);
    entity.contentType = getMediaTypeFromResponse(response, mime:APPLICATION_OCTET_STREAM);
    response.setEntity(entity);
}

@Description {value:"Set multiparts as the response payload"}
@Param {value:"response: The response message"}
@Param {value:"bodyParts: Represent body parts that needs to be set to the response"}
@Param {value:"contentType: Content type of the top level message"}
public function <OutResponse response> setMultiparts (mime:Entity[] bodyParts, string contentType) {
    mime:Entity entity = response.getEntityWithoutBody();
    mime:MediaType mediaType = getMediaTypeFromResponse(response, mime:MULTIPART_MIXED);
    if (contentType != null && contentType != "") {
        mediaType = mime:getMediaType(contentType);
    }
    entity.contentType = mediaType;
    entity.setBodyParts(bodyParts);
    response.setEntity(entity);
}

@Description {value:"Sets the entity body of the outbound response with the given file content"}
@Param {value:"response: The outbound response message"}
@Param {value:"fileHandler: File that needs to be set to the payload"}
@Param {value:"contentType: Content-Type of the file"}
public function <OutResponse response> setFileAsPayload(file:File fileHandler, string contentType) {
    mime:MediaType mediaType = mime:getMediaType(contentType);
    mime:Entity entity = response.getEntityWithoutBody();
    entity.contentType = mediaType;
    entity.setFileAsEntityBody(fileHandler);
    response.setEntity(entity);
}

@Description {value:"Sets a byte channel as the outbound response payload"}
@Param {value:"response: The outbound response message"}
@Param {value:"payload: The byte channel representation of the message payload"}
public function <OutResponse response> setByteChannel (io:ByteChannel payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setByteChannel(payload);
    response.setEntity(entity);
}

@Description {value:"Construct MediaType struct from the content-type header value"}
@Param {value:"response: The outbound response message"}
@Param {value:"defaultContentType: Default content-type to be used in case the content-type header doesn't contain any value"}
@Return {value:"Return 'MediaType' struct"}
function getMediaTypeFromResponse (OutResponse response, string defaultContentType) (mime:MediaType) {
    mime:MediaType mediaType = mime:getMediaType(defaultContentType);
    string contentType = response.getHeader(mime:CONTENT_TYPE);
    if (contentType != null && contentType != "") {
        mediaType = mime:getMediaType(contentType);
    }
    return mediaType;
}

package ballerina.net.http;

import ballerina.mime;
import ballerina.file;
import ballerina.io;

@Description { value:"Represents an HTTP response message"}
@Field {value:"statusCode: The response status code"}
@Field {value:"reasonPhrase: The status code reason phrase"}
@Field {value:"server: The server header"}
public struct Response {
    int statusCode;
    string reasonPhrase;
    string server;
}

//////////////////////////////
/// Native implementations ///
//////////////////////////////

@Description {value:"Get the entity from the response with the body"}
@Param {value:"res: The response message"}
@Return {value:"Entity of the response"}
@Return {value:"EntityError will might get thrown during entity construction in case of errors"}
public native function <Response res> getEntity () (mime:Entity, mime:EntityError);

@Description {value:"Get the entity from the response without the body"}
@Param {value:"req: The response message"}
@Return {value:"Entity of the response"}
public native function <Response res> getEntityWithoutBody () (mime:Entity);

@Description {value:"Retrieve a response property"}
@Param {value:"res: The response message"}
@Param {value:"propertyName: The name of the property"}
@Return {value:"The property value"}
public native function <Response res> getProperty (string propertyName) (string);

@Description {value:"Set the entity to response"}
@Param {value:"res: The response message"}
@Return {value:"Entity of the response"}
public native function <Response res> setEntity (mime:Entity entity);

@Description {value:"Sets a response property"}
@Param {value:"res: The response message"}
@Param {value:"propertyName: The name of the property"}
@Param {value:"propertyValue: The value of the property"}
public native function <Response res> setProperty (string propertyName, string propertyValue);

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"res: The response struct"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value struct for the provided header name. Returns null if the header does not exist."}
public function <Response res> getHeader (string headerName) (string) {
    mime:Entity entity = res.getEntityWithoutBody();
    return getFirstHeaderFromEntity(entity, headerName);
}

@Description {value:"Adds the specified key/value pair as an HTTP header to the outbound response"}
@Param {value:"res: The response message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Response res> addHeader (string headerName, string headerValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    addHeaderToEntity(entity, headerName, headerValue);
}

@Description {value:"Gets the HTTP headers from the inbound response"}
@Param {value:"res: The response message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <Response res> getHeaders (string headerName) (string[]) {
    mime:Entity entity = res.getEntityWithoutBody();
    return getHeadersFromEntity(entity, headerName);
}

@Description {value:"Sets the value of a transport header"}
@Param {value:"res: The response message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Response res> setHeader (string headerName, string headerValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    setHeaderToEntity(entity, headerName, headerValue);
}

@Description {value:"Removes a transport header from the response"}
@Param {value:"res: The response message"}
@Param {value:"key: The header name"}
public function <Response res> removeHeader (string key) {
    mime:Entity entity = res.getEntityWithoutBody();
    _ = entity.headers.remove(key);
}

@Description {value:"Removes all transport headers from the response"}
@Param {value:"res: The response message"}
public function <Response res> removeAllHeaders () {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.headers = {};
}

@Description {value:"Gets the Content-Length header value from the response"}
@Param {value:"response: The response message"}
@Return {value:"length of the message"}
public function <Response response> getContentLength () (int) {
    if (response.getHeader(CONTENT_LENGTH) != null) {
        string strContentLength = response.getHeader(CONTENT_LENGTH);
        return getContentLengthIntValue(strContentLength);
    }
    return -1;
}

@Description {value:"Gets the response payload in JSON format"}
@Param {value:"response: The response message"}
@Return {value:"The JSON reresentation of the message payload"}
public function <Response response> getJsonPayload () (json, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getJson();
}

@Description {value:"Gets the response payload in XML format"}
@Param {value:"response: The response message"}
@Return {value:"The XML representation of the message payload"}
public function <Response response> getXmlPayload () (xml, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getXml();
}

@Description {value:"Gets the response payload as a string"}
@Param {value:"response: The response message"}
@Return {value:"The string representation of the message payload"}
public function <Response response> getStringPayload () (string, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getText();
}

@Description {value:"Gets the response payload in blob format"}
@Param {value:"response: The response message"}
@Return {value:"The blob representation of the message payload"}
public function <Response response> getBinaryPayload () (blob, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        blob byteData;
        return byteData, entityError;
    }
    return entity.getBlob();
}

@Description {value:"Gets the response payload as a byte channel except for multiparts. In case of multiparts,
please use 'getMultiparts()' instead."}
@Param {value:"response: The response message"}
@Return {value:"A byte channel as the message payload"}
public function <Response response> getByteChannel () (io:ByteChannel, mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getByteChannel();
}

@Description {value:"Get multiparts from response"}
@Param {value:"response: The response message"}
@Return {value:"Returns the body parts as an array of entities"}
public function <Response response> getMultiparts () (mime:Entity[], mime:EntityError) {
    var entity, entityError = response.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getBodyParts();
}

@Description {value:"Sets a JSON as the outbound response payload"}
@Param {value:"response: The response message"}
@Param {value:"payload: The JSON payload object"}
public function <Response response> setJsonPayload (json payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setJson(payload);
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_JSON);
    entity.contentType = mediaType;
    response.setEntity(entity);
}

@Description {value:"Sets an XML as the outbound response payload"}
@Param {value:"response: The response message"}
@Param {value:"payload: The XML payload object"}
public function <Response response> setXmlPayload (xml payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setXml(payload);
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_XML);
    entity.contentType = mediaType;
    response.setEntity(entity);
}

@Description { value:"Sets a string as the outbound response payload"}
@Param { value:"response: The response message" }
@Param { value:"payload: The payload to be set to the response as a string" }
public function <Response response> setStringPayload (string payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setText(payload);
    mime:MediaType mediaType = mime:getMediaType(mime:TEXT_PLAIN);
    entity.contentType = mediaType;
    response.setEntity(entity);
}

@Description {value:"Sets a blob as the outbound response payload"}
@Param {value:"response: The response message"}
@Param {value:"payload: The blob representation of the message payload"}
public function <Response response> setBinaryPayload (blob payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setBlob(payload);
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    entity.contentType = mediaType;
    response.setEntity(entity);
}

@Description {value:"Set multiparts as the response payload"}
@Param {value:"response: The response message"}
@Param {value:"bodyParts: Represent body parts that needs to be set to the response"}
@Param {value:"contentType: Content type of the top level message"}
public function <Response response> setMultiparts (mime:Entity[] bodyParts, string contentType) {
    mime:Entity entity = response.getEntityWithoutBody();
    mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_MIXED);
    if (contentType != null && contentType != "") {
        mediaType = mime:getMediaType(contentType);
    }
    entity.contentType = mediaType;
    entity.setBodyParts(bodyParts);
    response.setEntity(entity);
}

@Description {value:"Sets the entity body of the outbound response with the given file content"}
@Param {value:"response: The response message"}
@Param {value:"fileHandler: File that needs to be set to the payload"}
@Param {value:"contentType: Content-Type of the file"}
public function <Response response> setFileAsPayload (file:File fileHandler, string contentType) {
    mime:MediaType mediaType = mime:getMediaType(contentType);
    mime:Entity entity = response.getEntityWithoutBody();
    entity.contentType = mediaType;
    entity.setFileAsEntityBody(fileHandler);
    response.setEntity(entity);
}

@Description {value:"Sets a byte channel as the outbound response payload"}
@Param {value:"response: The response message"}
@Param {value:"payload: The byte channel representation of the message payload"}
public function <Response response> setByteChannel (io:ByteChannel payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setByteChannel(payload);
    response.setEntity(entity);
}

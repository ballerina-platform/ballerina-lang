package ballerina.http;

import ballerina/file;
import ballerina/io;
import ballerina/mime;

@Description { value:"Represents an HTTP response message"}
@Field {value:"statusCode: The response status code"}
@Field {value:"reasonPhrase: The status code reason phrase"}
@Field {value:"server: The server header"}
@Field {value:"cacheControl: The cache control directives configuration of the response"}
@Field {value:"receivedTime: The time the response was received"}
@Field {value:"requestTime: The time the request associated with this response was made"}
public struct Response {
    int statusCode;
    string reasonPhrase;
    string server;
    ResponseCacheControl cacheControl;

    private:
        int receivedTime;
        int requestTime;
}

public function <Response response> Response() {
    response.cacheControl = {};
}
//////////////////////////////
/// Native implementations ///
//////////////////////////////

@Description {value:"Get the entity from the response with the body"}
@Param {value:"res: The response message"}
@Return {value:"Entity of the response"}
@Return {value:"EntityError will might get thrown during entity construction in case of errors"}
public native function <Response res> getEntity () returns (mime:Entity | mime:EntityError);

@Description {value:"Get the entity from the response without the body"}
@Param {value:"req: The response message"}
@Return {value:"Entity of the response"}
public native function <Response res> getEntityWithoutBody () returns (mime:Entity);

@Description {value:"Set the entity to response"}
@Param {value:"res: The response message"}
@Return {value:"Entity of the response"}
public native function <Response res> setEntity (mime:Entity entity);

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

@Description {value:"Check whether the requested header exists"}
@Param {value:"res: The response message"}
@Param {value:"headerName: The header name"}
@Return {value:"Boolean representing the existence of a given header"}
public function <Response res> hasHeader (string headerName) returns (boolean) {
    mime:Entity entity = res.getEntityWithoutBody();
    return entity.hasHeader(headerName);
}

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"res: The response struct"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value struct for the provided header name. Returns null if the header does not exist."}
public function <Response res> getHeader (string headerName) returns (string) {
    mime:Entity entity = res.getEntityWithoutBody();
    return entity.getHeader(headerName);
}

@Description {value:"Adds the specified key/value pair as an HTTP header to the outbound response"}
@Param {value:"res: The response message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Response res> addHeader (string headerName, string headerValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.addHeader(headerName, headerValue);
}

@Description {value:"Gets the HTTP headers from the inbound response"}
@Param {value:"res: The response message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <Response res> getHeaders (string headerName) returns (string[]) {
    mime:Entity entity = res.getEntityWithoutBody();
    return entity.getHeaders(headerName);
}

@Description {value:"Sets the value of a transport header"}
@Param {value:"res: The response message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Response res> setHeader (string headerName, string headerValue) {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.setHeader(headerName, headerValue);
}

@Description {value:"Removes a transport header from the response"}
@Param {value:"res: The response message"}
@Param {value:"key: The header name"}
public function <Response res> removeHeader (string key) {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.removeHeader(key);
}

@Description {value:"Removes all transport headers from the response"}
@Param {value:"res: The response message"}
public function <Response res> removeAllHeaders () {
    mime:Entity entity = res.getEntityWithoutBody();
    entity.removeAllHeaders();
}

@Description {value:"Get all transport headers from the response. Manipulating the return map does not have any impact to the original copy"}
@Param {value:"res: The response message"}
public function <Response res> getCopyOfAllHeaders () returns (map) {
    mime:Entity entity = res.getEntityWithoutBody();
    return entity.getCopyOfAllHeaders();
}

@Description {value:"Gets the response payload in JSON format"}
@Param {value:"response: The response message"}
@Return {value:"The JSON reresentation of the message payload or 'PayloadError' in case of errors"}
public function <Response response> getJsonPayload () returns (json | PayloadError) {
    match response.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getJson() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                json jsonPayload => return jsonPayload;
            }
        }
    }
}

@Description {value:"Gets the response payload in XML format"}
@Param {value:"response: The response message"}
@Return {value:"The XML representation of the message payload or 'PayloadError' in case of errors"}
public function <Response response> getXmlPayload () returns (xml | PayloadError) {
    match response.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getXml() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                xml xmlPayload => return xmlPayload;
            }
        }
    }
}

@Description {value:"Gets the response payload as a string"}
@Param {value:"response: The response message"}
@Return {value:"The string representation of the message payload or 'PayloadError' in case of errors"}
public function <Response response> getStringPayload () returns (string | PayloadError) {
    match response.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getText() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                string textPayload => return textPayload;
            }
        }
    }
}

@Description {value:"Gets the response payload in blob format"}
@Param {value:"response: The response message"}
@Return {value:"The blob representation of the message payload or 'PayloadError' in case of errors"}
public function <Response response> getBinaryPayload () returns (blob | PayloadError) {
    match response.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getBlob() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                blob binaryPayload => return binaryPayload;
            }
        }
    }
}

@Description {value:"Gets the response payload as a byte channel except for multiparts. In case of multiparts,
please use 'getMultiparts()' instead."}
@Param {value:"response: The response message"}
@Return {value:"A byte channel as the message payload or 'PayloadError' in case of errors"}
public function <Response response> getByteChannel () returns (io:ByteChannel | PayloadError) {
    match response.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getByteChannel() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                io:ByteChannel byteChannel => return byteChannel;
            }
        }
    }
}

@Description {value:"Get multiparts from response"}
@Param {value:"response: The response message"}
@Return {value:"Returns the body parts as an array of entities"}
public function <Response response> getMultiparts () returns mime:Entity[] | mime:EntityError {
    var mimeEntity = response.getEntity();
    match mimeEntity {
        mime:Entity entity => return entity.getBodyParts();
        mime:EntityError err => return err;
    }
}

@Description {value:"Sets a JSON as the outbound response payload"}
@Param {value:"response: The response message"}
@Param {value:"payload: The JSON payload object"}
public function <Response response> setJsonPayload (json payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setJson(payload);
    entity.contentType = getMediaTypeFromResponse(response, mime:APPLICATION_JSON);
    response.setEntity(entity);
}

@Description {value:"Sets an XML as the outbound response payload"}
@Param {value:"response: The response message"}
@Param {value:"payload: The XML payload object"}
public function <Response response> setXmlPayload (xml payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setXml(payload);
    entity.contentType = getMediaTypeFromResponse(response, mime:APPLICATION_XML);
    response.setEntity(entity);
}

@Description { value:"Sets a string as the outbound response payload"}
@Param { value:"response: The response message" }
@Param { value:"payload: The payload to be set to the response as a string" }
public function <Response response> setStringPayload (string payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setText(payload);
    entity.contentType = getMediaTypeFromResponse(response, mime:TEXT_PLAIN);
    response.setEntity(entity);
}

@Description {value:"Sets a blob as the outbound response payload"}
@Param {value:"response: The response message"}
@Param {value:"payload: The blob representation of the message payload"}
public function <Response response> setBinaryPayload (blob payload) {
    mime:Entity entity = response.getEntityWithoutBody();
    entity.setBlob(payload);
    entity.contentType = getMediaTypeFromResponse(response, mime:APPLICATION_OCTET_STREAM);
    response.setEntity(entity);
}

@Description {value:"Set multiparts as the response payload"}
@Param {value:"response: The response message"}
@Param {value:"bodyParts: Represent body parts that needs to be set to the response"}
@Param {value:"contentType: Content type of the top level message"}
public function <Response response> setMultiparts (mime:Entity[] bodyParts, string contentType) {
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

@Description {value:"Construct MediaType struct from the content-type header value"}
@Param {value:"response: The outbound response message"}
@Param {value:"defaultContentType: Default content-type to be used in case the content-type header doesn't contain any value"}
@Return {value:"Return 'MediaType' struct"}
function getMediaTypeFromResponse (Response response, string defaultContentType) returns (mime:MediaType) {
    mime:MediaType mediaType = mime:getMediaType(defaultContentType);

    if (response.hasHeader(mime:CONTENT_TYPE)) {
        string contentTypeValue = response.getHeader(mime:CONTENT_TYPE);
        if (contentTypeValue != "") { // TODO: may need to trim this before doing an empty string check
            return mime:getMediaType(contentTypeValue);
        } else {
            return mediaType;
        }
    } else {
        return mediaType;
    }
}

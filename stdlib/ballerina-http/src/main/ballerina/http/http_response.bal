package ballerina.http;

import ballerina/file;
import ballerina/io;
import ballerina/mime;
import ballerina/security.crypto;
import ballerina/time;

@Description { value:"Represents an HTTP response message"}
@Field {value:"statusCode: The response status code"}
@Field {value:"reasonPhrase: The status code reason phrase"}
@Field {value:"server: The server header"}
@Field {value:"resolvedRequestedURI: Ultimate request URI that was made to receive the response when redirect is on"}
@Field {value:"cacheControl: The cache control directives configuration of the response"}
public type Response object {
    public {
        int statusCode;
        string reasonPhrase;
        string server;
        string resolvedRequestedURI;
        ResponseCacheControl? cacheControl;
    }

    private {
        int receivedTime;
        int requestTime;
    }

    @Description {value:"Get the entity from the response with the body"}
    @Param {value:"res: The response message"}
    @Return {value:"Entity of the response"}
    @Return {value:"EntityError will might get thrown during entity construction in case of errors"}
    public native function getEntity () returns (mime:Entity | mime:EntityError);

    @Description {value:"Get the entity from the response without the body"}
    @Param {value:"req: The response message"}
    @Return {value:"Entity of the response"}
    public native function getEntityWithoutBody () returns (mime:Entity);

    @Description {value:"Set the entity to response"}
    @Param {value:"res: The response message"}
    @Return {value:"Entity of the response"}
    public native function setEntity (mime:Entity entity);

    @Description {value:"Check whether the requested header exists"}
    @Param {value:"res: The response message"}
    @Param {value:"headerName: The header name"}
    @Return {value:"Boolean representing the existence of a given header"}
    public function hasHeader (string headerName) returns (boolean);

    @Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
    @Param {value:"res: The response struct"}
    @Param {value:"headerName: The header name"}
    @Return {value:"The first header value struct for the provided header name. Returns null if the header does not exist."}
    public function getHeader (string headerName) returns (string);

    @Description {value:"Adds the specified key/value pair as an HTTP header to the outbound response"}
    @Param {value:"res: The response message"}
    @Param {value:"headerName: The header name"}
    @Param {value:"headerValue: The header value"}
    public function addHeader (string headerName, string headerValue);

    @Description {value:"Gets the HTTP headers from the inbound response"}
    @Param {value:"res: The response message"}
    @Param {value:"headerName: The header name"}
    @Return {value:"The header values struct array for a given header name"}
    public function getHeaders (string headerName) returns (string[]);

    @Description {value:"Sets the value of a transport header"}
    @Param {value:"res: The response message"}
    @Param {value:"headerName: The header name"}
    @Param {value:"headerValue: The header value"}
    public function setHeader (string headerName, string headerValue);

    @Description {value:"Removes a transport header from the response"}
    @Param {value:"res: The response message"}
    @Param {value:"key: The header name"}
    public function removeHeader (string key);

    @Description {value:"Removes all transport headers from the response"}
    @Param {value:"res: The response message"}
    public function removeAllHeaders ();

    @Description {value:"Get all transport header names from the response."}
    @Return {value:"An array of all transport header names"}
    public function getHeaderNames () returns (string[]);

    @Description {value:"Set the content-type header to response"}
    @Param {value:"contentType: Content type value that needs to be set to Content-Type header"}
    public function setContentType (string contentType);

    @Description {value:"Get the content-type value from the response"}
    @Return {value:"Returns the content-type header value as a string."}
    public function getContentType () returns (string?);

    @Description {value:"Gets the response payload in JSON format"}
    @Param {value:"response: The response message"}
    @Return {value:"The JSON reresentation of the message payload or 'PayloadError' in case of errors"}
    public function getJsonPayload () returns (json | PayloadError);

    @Description {value:"Gets the response payload in XML format"}
    @Param {value:"response: The response message"}
    @Return {value:"The XML representation of the message payload or 'PayloadError' in case of errors"}
    public function getXmlPayload () returns (xml | PayloadError);

    @Description {value:"Gets the response payload as a string"}
    @Param {value:"response: The response message"}
    @Return {value:"The string representation of the message payload or 'PayloadError' in case of errors"}
    public function getStringPayload () returns (string | PayloadError);

    @Description {value:"Gets the response payload in blob format"}
    @Param {value:"response: The response message"}
    @Return {value:"The blob representation of the message payload or 'PayloadError' in case of errors"}
    public function getBinaryPayload () returns (blob | PayloadError);

    @Description {value:"Gets the response payload as a byte channel except for multiparts. In case of multiparts,
    please use 'getBodyParts()' instead."}
    @Param {value:"response: The response message"}
    @Return {value:"A byte channel as the message payload or 'PayloadError' in case of errors"}
    public function getByteChannel () returns (io:ByteChannel | PayloadError);

    @Description {value:"Get multiparts from response"}
    @Param {value:"response: The response message"}
    @Return {value:"Returns the body parts as an array of entities"}
    public function getBodyParts () returns (mime:Entity[] | mime:EntityError);

    @Description {value:"Sets the ETag header for the given payload. The ETag is generated using a CRC32 hash function."}
    @Param {value:"The payload for which the ETag should be set."}
    public function setETag(json|xml|string|blob payload);

    @Description {value:"Sets the current time as the Last-Modified header."}
    public function setLastModified();

    @Description {value:"Sets a JSON as the outbound response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"payload: The JSON payload object"}
    public function setJsonPayload (json payload);

    @Description {value:"Sets an XML as the outbound response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"payload: The XML payload object"}
    public function setXmlPayload (xml payload);

    @Description { value:"Sets a string as the outbound response payload"}
    @Param { value:"response: The response message" }
    @Param { value:"payload: The payload to be set to the response as a string" }
    public function setStringPayload (string payload);

    @Description {value:"Sets a blob as the outbound response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"payload: The blob representation of the message payload"}
    public function setBinaryPayload (blob payload);

    @Description {value:"Set multiparts as the response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"bodyParts: Represent body parts that needs to be set to the response"}
    @Param {value:"contentType: Content type of the top level message"}
    public function setBodyParts (mime:Entity[] bodyParts, string contentType);

    @Description {value:"Sets the entity body of the outbound response with the given file content"}
    @Param {value:"response: The response message"}
    @Param {value:"filePath: Path to the file that needs to be set to the payload"}
    @Param {value:"contentType: Content-Type of the file"}
    public function setFileAsPayload (string filePath, string contentType);

    @Description {value:"Sets a byte channel as the outbound response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"payload: The byte channel representation of the message payload"}
    public function setByteChannel (io:ByteChannel payload);

    @Description {value:"Set the response payload"}
    @Param {value:"payload: Payload can be of type string, xml, json, blob, byte channel or set of body parts"}
    public function setPayload ((string | xml | json | blob | io:ByteChannel | mime:Entity[]) payload);
};

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

public function Response::hasHeader (string headerName) returns (boolean) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.hasHeader(headerName);
}

public function Response::getHeader (string headerName) returns (string) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeader(headerName);
}

public function Response::addHeader (string headerName, string headerValue) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.addHeader(headerName, headerValue);
}

public function Response::getHeaders (string headerName) returns (string[]) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeaders(headerName);
}

public function Response::setHeader (string headerName, string headerValue) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setHeader(headerName, headerValue);
}

public function Response::removeHeader (string key) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.removeHeader(key);
}

public function Response::removeAllHeaders () {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.removeAllHeaders();
}

public function Response::getHeaderNames () returns (string[]) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeaderNames();
}

public function Response::setContentType (string contentType) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setHeader(mime:CONTENT_TYPE, contentType);
}

public function Response::getContentType () returns (string?) {
    if (self.hasHeader(mime:CONTENT_TYPE)) {
        return self.getHeader(mime:CONTENT_TYPE);
    }
    return ();
}

public function Response::getJsonPayload () returns (json | PayloadError) {
    match self.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getJson() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                json jsonPayload => return jsonPayload;
            }
        }
    }
}

public function Response::getXmlPayload () returns (xml | PayloadError) {
    match self.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getXml() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                xml xmlPayload => return xmlPayload;
            }
        }
    }
}

public function Response::getStringPayload () returns (string | PayloadError) {
    match self.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getText() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                string textPayload => return textPayload;
            }
        }
    }
}

public function Response::getBinaryPayload () returns (blob | PayloadError) {
    match self.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getBlob() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                blob binaryPayload => return binaryPayload;
            }
        }
    }
}

public function Response::getByteChannel () returns (io:ByteChannel | PayloadError) {
    match self.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getByteChannel() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                io:ByteChannel byteChannel => return byteChannel;
            }
        }
    }
}

public function Response::getBodyParts () returns mime:Entity[] | mime:EntityError {
    var mimeEntity = self.getEntity();
    match mimeEntity {
        mime:Entity entity => return entity.getBodyParts();
        mime:EntityError err => return err;
    }
}

public function Response::setETag(json|xml|string|blob payload) {
    string etag = crypto:getCRC32(payload);
    self.setHeader(ETAG, etag);
}

public function Response::setLastModified() {
    time:Time currentT = time:currentTime();
    string lastModified = currentT.format(time:TIME_FORMAT_RFC_1123);
    self.setHeader(LAST_MODIFIED, lastModified);
}

public function Response::setJsonPayload (json payload) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setJson(payload);
    entity.contentType = getMediaTypeFromResponse(self, mime:APPLICATION_JSON);
    self.setEntity(entity);
}

public function Response::setXmlPayload (xml payload) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setXml(payload);
    entity.contentType = getMediaTypeFromResponse(self, mime:APPLICATION_XML);
    self.setEntity(entity);
}

public function Response::setStringPayload (string payload) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setText(payload);
    entity.contentType = getMediaTypeFromResponse(self, mime:TEXT_PLAIN);
    self.setEntity(entity);
}

public function Response::setBinaryPayload (blob payload) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBlob(payload);
    entity.contentType = getMediaTypeFromResponse(self, mime:APPLICATION_OCTET_STREAM);
    self.setEntity(entity);
}

public function Response::setBodyParts (mime:Entity[] bodyParts, @sensitive string contentType) {
    mime:Entity entity = self.getEntityWithoutBody();
    mime:MediaType mediaType = getMediaTypeFromResponse(self, mime:MULTIPART_MIXED);
    if (contentType != null && contentType != "") {
        mediaType = mime:getMediaType(contentType);
    }
    entity.contentType = mediaType;
    entity.setBodyParts(bodyParts);
    self.setEntity(entity);
}

public function Response::setFileAsPayload (string filePath, @sensitive string contentType) {
    mime:MediaType mediaType = mime:getMediaType(contentType);
    mime:Entity entity = self.getEntityWithoutBody();
    entity.contentType = mediaType;
    entity.setFileAsEntityBody(filePath);
    self.setEntity(entity);
}

public function Response::setByteChannel (io:ByteChannel payload) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setByteChannel(payload);
    self.setEntity(entity);
}

public function Response::setPayload ((string | xml | json | blob | io:ByteChannel | mime:Entity[]) payload) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBody(payload);
    self.setEntity(entity);
}

@Description {value:"Construct MediaType struct from the content-type header value"}
@Param {value:"response: The outbound response message"}
@Param {value:"defaultContentType: Default content-type to be used in case the content-type header doesn't contain any value"}
@Return {value:"Return 'MediaType' struct"}
function getMediaTypeFromResponse (Response response, @sensitive string defaultContentType) returns (mime:MediaType) {
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

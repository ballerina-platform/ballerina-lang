
import ballerina/file;
import ballerina/io;
import ballerina/mime;
import ballerina/crypto;
import ballerina/time;

@Description { value:"Represents an HTTP response message"}
@Field {value:"statusCode: The response status code"}
@Field {value:"reasonPhrase: The status code reason phrase"}
@Field {value:"server: The server header"}
@Field {value:"cacheControl: The cache control directives configuration of the response"}
public type Response object {
    public {
        int statusCode;
        string reasonPhrase;
        string server;
        ResponseCacheControl? cacheControl;
    }

    private {
        int receivedTime;
        int requestTime;
    }

    documentation {
        Get the `mime:Entity` from the response.

        R{{}} Return a `mime:Entity` or an `error` record in case of errors
    }
    public native function getEntity () returns (mime:Entity | error);

    documentation {
        Get the `mime:Entity` without populating its body from the response.

        R{{}} Return a `mime:Entity`
    }
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
    public function getContentType () returns (string);

    documentation {
        Extract json payload from the response. If the payload is not json compatible an error will be returned.

        R{{}} Return `json` payload extracted from the response. An `error` record will be returned in case of
        errors.
    }
    public function getJsonPayload () returns (json | error);

    documentation {
        Extract xml payload from the response. If the payload is not xml compatible an error will be returned.

        R{{}} Return `xml` payload extracted from the the response. An `error` record will be returned in case of
        errors.
    }
    public function getXmlPayload () returns (xml | error);

    documentation {
        Extract text payload from the response. If the payload is not text compatible an error will be returned.

        R{{}} Return `string` request extracted from the the response. An `error` record will be returned in case of
        errors.
    }
    public function getTextPayload () returns (string | error);

    documentation {
        Get the response payload as a string. Content-type will not be checked during payload construction which
        makes this different from getTextPayload() method.

        R{{}} The string representation of the message payload or an error record in case of errors
    }
    public function getPayloadAsString () returns (string | error);

    documentation {
        Get the binary payload from the response. If the payload is considerably large consider using getByteChannel()
        method instead.

        R{{}} Return `blob` data extracted from the the response. An `error` record will be returned in case of
        errors.
    }
    public function getBinaryPayload () returns (blob | error);

    documentation {
        Get a byte channel from the response. In case of multiparts, please use 'getBodyParts()' method instead.

        R{{}} Return an `io:ByteChannel`. An `error` record will be returned in case of errors
    }
    public function getByteChannel () returns (io:ByteChannel | error);

    documentation {
        Get multiparts from the response. If the payload is not a set of body parts an error will be returned.

        R{{}} Return an array of body parts(`Entity[]`) extracted from the response. An `error` record will be
        returned in case of errors
    }
    public function getBodyParts () returns (mime:Entity[] | error);

    @Description {value:"Sets the ETag header for the given payload. The ETag is generated using a CRC32 hash function."}
    @Param {value:"The payload for which the ETag should be set."}
    public function setETag(json|xml|string|blob payload);

    @Description {value:"Sets the current time as the Last-Modified header."}
    public function setLastModified();

    @Description {value:"Sets a JSON as the outbound response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"payload: The JSON payload object"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setJsonPayload(json payload, string contentType = "application/json");

    @Description {value:"Sets an XML as the outbound response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"payload: The XML payload object"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setXmlPayload(xml payload, string contentType = "application/xml");

    @Description { value:"Sets a text as the outbound response payload"}
    @Param { value:"response: The response message" }
    @Param { value:"payload: The payload to be set to the response as a string" }
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setTextPayload(string payload, string contentType = "text/plain");

    @Description {value:"Sets a blob as the outbound response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"payload: The blob representation of the message payload"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setBinaryPayload(blob payload, string contentType = "application/octec-stream");

    @Description {value:"Set multiparts as the response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"bodyParts: Represent body parts that needs to be set to the response"}
    @Param {value:"contentType: Content type of the top level message"}
    public function setBodyParts(mime:Entity[] bodyParts, string contentType = "multipart/form-data");

    @Description {value:"Sets the entity body of the outbound response with the given file content"}
    @Param {value:"response: The response message"}
    @Param {value:"filePath: Path to the file that needs to be set to the payload"}
    @Param {value:"contentType: Content-Type of the file"}
    public function setFileAsPayload (string filePath, string contentType = "application/octec-stream");

    @Description {value:"Sets a byte channel as the outbound response payload"}
    @Param {value:"response: The response message"}
    @Param {value:"payload: The byte channel representation of the message payload"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setByteChannel(io:ByteChannel payload, string contentType = "application/octec-stream");

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

public function Response::getContentType () returns (string) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getContentType();
}

public function Response::getJsonPayload () returns (json | error) {
    match self.getEntity() {
        error err => return err;
        mime:Entity mimeEntity => {
            match mimeEntity.getJson() {
                error payloadErr => return payloadErr;
                json jsonPayload => return jsonPayload;
            }
        }
    }
}

public function Response::getXmlPayload () returns (xml | error) {
    match self.getEntity() {
        error err => return err;
        mime:Entity mimeEntity => {
            match mimeEntity.getXml() {
                error payloadErr => return payloadErr;
                xml xmlPayload => return xmlPayload;
            }
        }
    }
}

public function Response::getTextPayload () returns (string | error) {
    match self.getEntity() {
        error err => return err;
        mime:Entity mimeEntity => {
            match mimeEntity.getText() {
                error payloadErr => return payloadErr;
                string textPayload => return textPayload;
            }
        }
    }
}

public function Response::getPayloadAsString () returns (string | error) {
    match self.getEntity() {
        error err => return err;
        mime:Entity mimeEntity => {
            match mimeEntity.getBodyAsString() {
                error payloadErr => return payloadErr;
                string stringPayload => return stringPayload;
            }
        }
    }
}

public function Response::getBinaryPayload () returns (blob | error) {
    match self.getEntity() {
        error err => return err;
        mime:Entity mimeEntity => {
            match mimeEntity.getBlob() {
                error payloadErr => return payloadErr;
                blob binaryPayload => return binaryPayload;
            }
        }
    }
}

public function Response::getByteChannel () returns (io:ByteChannel | error) {
    match self.getEntity() {
        error err => return err;
        mime:Entity mimeEntity => {
            match mimeEntity.getByteChannel() {
                error payloadErr => return payloadErr;
                io:ByteChannel byteChannel => return byteChannel;
            }
        }
    }
}

public function Response::getBodyParts () returns mime:Entity[] | error {
    var mimeEntity = self.getEntity();
    match mimeEntity {
        mime:Entity entity => return entity.getBodyParts();
        error err => return err;
    }
}

public function Response::setETag(json|xml|string|blob payload) {
    string etag = crypto:crc32(payload);
    self.setHeader(ETAG, etag);
}

public function Response::setLastModified() {
    time:Time currentT = time:currentTime();
    string lastModified = currentT.format(time:TIME_FORMAT_RFC_1123);
    self.setHeader(LAST_MODIFIED, lastModified);
}

public function Response::setJsonPayload (json payload, string contentType="application/json") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setJson(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setXmlPayload (xml payload, string contentType="application/xml") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setXml(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setTextPayload (string payload, string contentType="text/plain") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setText(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setBinaryPayload (blob payload, string contentType="application/octec-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBlob(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setBodyParts (mime:Entity[] bodyParts, string contentType="multipart/form-data") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBodyParts(bodyParts, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setFileAsPayload (string filePath, @sensitive string contentType = "application/octec-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setFileAsEntityBody(filePath, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setByteChannel (io:ByteChannel payload, string contentType="application/octec-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setByteChannel(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setPayload ((string | xml | json | blob | io:ByteChannel | mime:Entity[]) payload) {
    match payload {
        string textContent => self.setTextPayload(textContent);
        xml xmlContent => self.setXmlPayload(xmlContent);
        json jsonContent => self.setJsonPayload(jsonContent);
        blob blobContent => self.setBinaryPayload(blobContent);
        io:ByteChannel byteChannelContent => self.setByteChannel(byteChannelContent);
        mime:Entity[] bodyParts => self.setBodyParts(bodyParts);
    }
}


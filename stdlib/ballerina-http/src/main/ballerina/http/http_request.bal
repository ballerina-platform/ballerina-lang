package ballerina.http;

import ballerina/file;
import ballerina/io;
import ballerina/mime;

@Description { value:"Represents an HTTP request message"}
@Field {value:"path: Resource path of request URI"}
@Field {value:"method: HTTP request method"}
@Field {value:"httpVersion: The version of HTTP"}
@Field {value:"userAgent: User-Agent request header"}
@Field {value:"extraPathInfo: Additional information associated with the URL sent by the client"}
public type Request object {
    public {
        string rawPath;
        string method;
        string httpVersion;
        string userAgent;
        string extraPathInfo;
        RequestCacheControl? cacheControl;
    }

    @Description {value:"Set the entity to request"}
    @Return {value:"Entity of the request"}
    public native function setEntity (mime:Entity entity);

    @Description {value:"Gets the query parameters from the HTTP request as a map"}
    @Return {value:"The map of query params"}
    public native function getQueryParams () returns (map);

    @Description {value:"Get matrix parameters from the request"}
    @Param {value:"path: Path to the location of matrix parameters"}
    @Return {value:"A map of matrix paramters which can be found for a given path"}
    public native function getMatrixParams (string path) returns (map);

    @Description {value:"Get the entity from the request"}
    @Return {value:"Entity of the request"}
    @Return {value:"EntityError will might get thrown during entity construction in case of errors"}
    public native function getEntity () returns (mime:Entity | mime:EntityError);

    @Description {value:"Get the entity from the request without the body. This function is to be used only internally"}
    @Return {value:"Entity of the request"}
    native function getEntityWithoutBody () returns (mime:Entity);

    @Description {value:"Check whether the requested header exists"}
    @Param {value:"headerName: The header name"}
    @Return {value:"Boolean representing the existence of a given header"}
    public function hasHeader (string headerName) returns (boolean);

    @Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
    @Param {value:"headerName: The header name"}
    @Return {value:"The first header value for the provided header name. Returns null if the header does not exist."}
    public function getHeader (string headerName) returns (string);

    @Description {value:"Gets transport headers from the request"}
    @Param {value:"headerName: The header name"}
    @Return {value:"The header values struct array for a given header name"}
    public function getHeaders (string headerName) returns (string[]);

    @Description {value:"Sets the value of a transport header"}
    @Param {value:"headerName: The header name"}
    @Param {value:"headerValue: The header value"}
    public function setHeader (string headerName, string headerValue);

    @Description {value:"Adds the specified key/value pair as an HTTP header to the request"}
    @Param {value:"headerName: The header name"}
    @Param {value:"headerValue: The header value"}
    public function addHeader (string headerName, string headerValue);

    @Description {value:"Removes a transport header from the request"}
    @Param {value:"key: The header name"}
    public function removeHeader (string key);

    @Description {value:"Removes all transport headers from the message"}
    public function removeAllHeaders ();

    @Description {value:"Get all transport header names from the request."}
    @Return {value:"An array of all transport header names"}
    public function getHeaderNames () returns (string[]);

    @Description {value:"Checks whether the client expects a 100-continue response."}
    @Return {value:"Returns true if the client expects a 100-continue response. If not, returns false."}
    public function expects100Continue () returns (boolean);

    @Description {value:"Set the content-type header to request"}
    @Param {value:"contentType: Content type value that needs to be set to Content-Type header"}
    public function setContentType (string contentType);

    @Description {value:"Get the content-type value from the request"}
    @Return {value:"Returns the content-type header value as a string."}
    public function getContentType () returns (string);

    @Description {value:"Gets the request payload in JSON format"}
    @Return {value:"The JSON reresentation of the message payload or 'PayloadError' in case of errors"}
    public function getJsonPayload () returns (json | PayloadError);

    @Description {value:"Gets the request payload in XML format"}
    @Return {value:"The XML representation of the message payload or 'PayloadError' in case of errors"}
    public function getXmlPayload () returns (xml | PayloadError);

    @Description {value:"Gets the request payload as a string"}
    @Return {value:"The string representation of the message payload or 'PayloadError' in case of errors"}
    public function getStringPayload () returns (string | PayloadError);

    @Description {value:"Gets the request payload in blob format"}
    @Return {value:"The blob representation of the message payload or 'PayloadError' in case of errors"}
    public function getBinaryPayload () returns (blob | PayloadError);

    @Description {value:"Get the request payload as a byte channel except for multiparts. In case of multiparts,
    please use 'getBodyParts()' instead."}
    @Return {value:"A byte channel as the message payload or 'PayloadError' in case of errors"}
    public function getByteChannel () returns (io:ByteChannel | PayloadError);

    @Description {value:"Gets the form parameters from the HTTP request as a map"}
    @Return {value:"The map of form params or 'PayloadError' in case of errors"}
    public function getFormParams () returns (map<string> | PayloadError);

    @Description {value:"Get multiparts from request"}
    @Return {value:"Returns the body parts as an array of entities"}
    public function getBodyParts () returns (mime:Entity[] | mime:EntityError);

    @Description {value:"Sets a JSON as the request payload"}
    @Param {value:"payload: The JSON payload to be set to the request"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setJsonPayload(json payload, string contentType = "application/json");

    @Description {value:"Sets an XML as the payload"}
    @Param {value:"payload: The XML payload object"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setXmlPayload(xml payload, string contentType = "application/xml");

    @Description {value:"Sets a string as the request payload"}
    @Param {value:"payload: The payload to be set to the request as a string"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setStringPayload(string payload, string contentType = "text/plain");

    @Description {value:"Sets a blob as the request payload"}
    @Param {value:"payload: The blob representation of the message payload"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setBinaryPayload(blob payload, string contentType = "application/octec-stream");

    @Description {value:"Set multiparts as the request payload"}
    @Param {value:"bodyParts: Represent body parts that needs to be set to the request"}
    @Param {value:"contentType: Content type of the top level message"}
    public function setBodyParts (mime:Entity[] bodyParts, string contentType="multipart/form-data");

    @Description {value:"Sets the entity body of the request with the given file content"}
    @Param {value:"filePath: Path to the file that needs to be set to the payload"}
    @Param {value:"contentType: Content-Type of the given file"}
    public function setFileAsPayload (string filePath, string contentType = "application/octec-stream");

    @Description {value:"Set a byte channel as the request payload"}
    @Param {value:"payload: The byte channel representation of the message payload"}
    @Param {value:"contentType: Represent the content-type to be used with the payload"}
    public function setByteChannel(io:ByteChannel payload, string contentType = "application/octec-stream");

    @Description {value:"Set the request payload"}
    @Param {value:"payload: Payload can be of type string, xml, json, blob or byte channel"}
    public function setPayload ((string | xml | json | blob | io:ByteChannel | mime:Entity[]) payload);

    function parseCacheControlHeader();
};

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

public function Request::hasHeader (string headerName) returns (boolean) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.hasHeader(headerName);
}

public function Request::getHeader (string headerName) returns (string) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeader(headerName);
}

public function Request::getHeaders (string headerName) returns (string[]) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeaders(headerName);
}

public function Request::setHeader (string headerName, string headerValue) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setHeader(headerName, headerValue);
}

public function Request::addHeader (string headerName, string headerValue) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.addHeader(headerName, headerValue);
}

public function Request::removeHeader (string key) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.removeHeader(key);
}

public function Request::removeAllHeaders () {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.removeAllHeaders();
}

public function Request::getHeaderNames () returns (string[]) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeaderNames();
}

public function Request::expects100Continue () returns (boolean) {
    return self.hasHeader(EXPECT) ? self.getHeader(EXPECT) ==  "100-continue" : false;
}

public function Request::setContentType (string contentType) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setContentType(contentType);
}

public function Request::getContentType () returns (string) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.getContentType();
}

public function Request::getJsonPayload () returns (json | PayloadError) {
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

public function Request::getXmlPayload () returns (xml | PayloadError) {
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

public function Request::getStringPayload () returns (string | PayloadError) {
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

public function Request::getBinaryPayload () returns (blob | PayloadError) {
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

public function Request::getByteChannel () returns (io:ByteChannel | PayloadError) {
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

public function Request::getFormParams () returns (map<string> | PayloadError) {
    var mimeEntity = self.getEntity();
    match mimeEntity {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity entity => {

            map<string> parameters;
            var entityText = entity.getText();
            match entityText {
                mime:EntityError txtErr => return <PayloadError>txtErr; // TODO: Check if this is ok

                string formData => {
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
                }
            }
            return parameters;
        }
    }
}

public function Request::getBodyParts () returns (mime:Entity[] | mime:EntityError) {
    var mimeEntity = self.getEntity();
    match mimeEntity {
        mime:Entity entity => return entity.getBodyParts();
        mime:EntityError err => return err;
    }
}

public function Request::setJsonPayload (json payload, string contentType="application/json") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setJson(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setXmlPayload (xml payload, string contentType="application/xml") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setXml(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setStringPayload (string payload, string contentType="text/plain") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setText(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setBinaryPayload (blob payload, string contentType="application/octec-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBlob(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setBodyParts (mime:Entity[] bodyParts, string contentType="multipart/form-data") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBodyParts(bodyParts, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setFileAsPayload (string filePath, @sensitive string contentType="application/octec-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setFileAsEntityBody(filePath, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setByteChannel (io:ByteChannel payload, string contentType="application/octec-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setByteChannel(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setPayload ((string | xml | json | blob | io:ByteChannel | mime:Entity[]) payload) {
    match payload {
        string textContent => self.setStringPayload(textContent);
        xml xmlContent => self.setXmlPayload(xmlContent);
        json jsonContent => self.setJsonPayload(jsonContent);
        blob blobContent => self.setBinaryPayload(blobContent);
        io:ByteChannel byteChannelContent => self.setByteChannel(byteChannelContent);
        mime:Entity[] bodyParts => self.setBodyParts(bodyParts);
    }
}


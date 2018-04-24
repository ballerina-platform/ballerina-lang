// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/file;
import ballerina/io;
import ballerina/mime;

documentation {
    Represents an HTTP request.

    F{{rawPath}} Resource path of the request URL
    F{{method}} The HTTP request method
    F{{httpVersion}} The HTTP version supported by the client
    F{{userAgent}} The user-agent. This value is used when setting the `user-agent` header
    F{{extraPathInfo}} Additional information associated with the URL provided by the client
    F{{cacheControl}} The cache-control directives for the request. This needs to be explicitly initialized if intending
                      on utilizing HTTP caching.
}
public type Request object {

    public {
        string rawPath;
        string method;
        string httpVersion;
        string userAgent;
        string extraPathInfo;
        RequestCacheControl? cacheControl;
    }

    documentation {
        Sets the provided `Entity` to the request.

        P{{entity}} The `Entity` to be set to the request
    }
    public native function setEntity(mime:Entity entity);

    documentation {
        Gets the query parameters of the request, as a map.

        R{{}}  String map of query params
    }
    public native function getQueryParams() returns (map<string>);

    documentation {
        Gets the matrix parameters of the request.

        P{{path}} Path to the location of matrix parameters
        R{{}} A map of matrix paramters which can be found for the given path
    }
    public native function getMatrixParams(string path) returns (map);

    documentation {
        Gets the `Entity` associated with the request.

        R{{}} The `Entity` of the request. `EntityError` will be returned if entity construction fails.
    }
    public native function getEntity() returns mime:Entity|mime:EntityError;

    // Gets the `Entity` from the request without the body. This function is only for use within the package.
    native function getEntityWithoutBody() returns (mime:Entity);

    documentation {
        Checks whether the requested header key exists in the header map.

        P{{headerName}} The header name
        R{{}} Returns true if the specified header key exists
    }
    public function hasHeader(string headerName) returns (boolean);

    documentation {
        Returns the value of the specified header. If the specified header key maps to multiple values, the first of
        these values is returned.

        P{{headerName}} The header name
        R{{}} The first header value for the specified header name. Returns an empty string if the header does not
              exist.
    }
    public function getHeader(string headerName) returns (string);

    documentation {
        Gets all the header values to which the specified header key maps to.

        P{{headerName}} The header name
        R{{}} The header values the specified header key maps to
    }
    public function getHeaders(string headerName) returns (string[]);

    documentation {
        Sets the specified header to the request. If a mapping already exists for the specified header key, the existing
        header value is replaced with the specfied header value.

        P{{headerName}} The header name
        P{{headerValue}} The header value
    }
    public function setHeader(string headerName, string headerValue);

    documentation {
        Adds the specified header to the request. Existing header values are not replaced.

        P{{headerName}} The header name
        P{{headerValue}} The header value
    }
    public function addHeader(string headerName, string headerValue);

    documentation {
        Removes the specified header from the request.

        P{{key}} The header name
    }
    public function removeHeader(string key);

    documentation {
        Removes all the headers from the request.
    }
    public function removeAllHeaders();

    documentation {
        Gets all the names of the headers of the request.

        R{{}} An array of all the header names
    }
    public function getHeaderNames() returns (string[]);

    documentation {
        Checks whether the client expects a `100-continue` response.

        R{{}} Returns true if the client expects a `100-continue` response
    }
    public function expects100Continue() returns (boolean);

    documentation {
        Sets the `content-type` header to the request.

        P{{contentType}} Content type value to be set as the `content-type` header
    }
    public function setContentType(string contentType);

    documentation {
        Gets the type of the payload of the request (i.e: the `content-type` header value).

        R{{}} Returns the `content-type` header value as a string
    }
    public function getContentType() returns (string);

    documentation {
        Gets the request payload as a `json`.

        R{{}} The JSON reresentation of the message payload or `PayloadError` in case of errors
    }
    public function getJsonPayload() returns json|PayloadError;

    @Description {value:"Get the text payload from the request"}
    @Return {value:"The string representation of the message payload or 'PayloadError' in case of errors"}
    public function getTextPayload () returns (string | PayloadError);

    documentation {
        Gets the request payload as a `string`.

        R{{}} The string representation of the message payload or `PayloadError` in case of errors
    }
    public function getPayloadAsString () returns (string | PayloadError);

    documentation {
        Gets the request payload as an `xml`.

        R{{}} The XML representation of the message payload or `PayloadError` in case of errors
    }
    public function getXmlPayload() returns xml|PayloadError;

    documentation {
        Gets the request payload as a `ByteChannel` except in the case of multiparts. To retrieve multiparts, use
        `getBodyParts()`.

        R{{}} A byte channel from which the message payload can be read or `PayloadError` in case of errors
    }
    public function getByteChannel () returns (io:ByteChannel | PayloadError);

    documentation {
        Gets the request payload as a `blob`.

        R{{}} The blob representation of the message payload or `PayloadError` in case of errors
    }
    public function getBinaryPayload() returns blob|PayloadError;

    documentation {
        Gets the form parameters from the HTTP request as a `map`.

        R{{}} The map of form params or `PayloadError` in case of errors
    }
    public function getFormParams () returns (map<string> | PayloadError);

    documentation {
        Get multiparts from request.

        R{{}} Returns the body parts as an array of entities or an `EntityError` if there were any errors in
              constructing the body parts from the request
    }
    public function getBodyParts () returns (mime:Entity[] | mime:EntityError);

    documentation {
        Sets a `json` as the payload.

        P{{payload}} The `json` payload
        P{{contentType}} The content type of the payload. Set this to override the default `content-type` header value
                         for `json`.
    }
    public function setJsonPayload(json payload, string contentType = "application/json");

    documentation {
        Sets an `xml` as the payload

        P{{payload}} The `xml` payload
        P{{contentType}} The content type of the payload. Set this to override the default `content-type` header value
                         for `xml`.
    }
    public function setXmlPayload(xml payload, string contentType = "application/xml");

    documentation {
        Sets a `string` as the payload.

        P{{payload}} The `string` payload
        P{{contentType}} The content type of the payload. Set this to override the default `content-type` header value
                         for `string`.
    }
    public function setTextPayload(string payload, string contentType = "text/plain");

    documentation {
        Sets a `blob` as the payload.

        P{{payload}} The `blob` payload
        P{{contentType}} The content type of the payload. Set this to override the default `content-type` header value
                         for `blob`.
    }
    public function setBinaryPayload(blob payload, string contentType = "application/octec-stream");

    documentation {
        Set multiparts as the payload.

        P{{bodyParts}} The entities which make up the message body
        P{{contentType}} The content type of the top level message. Set this to override the default
                         `content-type` header value.
    }
    public function setBodyParts(mime:Entity[] bodyParts, string contentType="multipart/form-data");

    documentation {
        Sets the content of the specified file as the entity body of the request.

        P{{filePath}} Path to the file to be set as the payload
        P{{contentType}} The content type of the specified file. Set this to override the default `content-type`
                         header value.
    }
    public function setFileAsPayload (string filePath, string contentType = "application/octec-stream");

    documentation {
        Sets a `ByteChannel` as the payload.

        P{{payload}} A `ByteChannel` through which the message payload can be read
        P{{contentType}} The content type of the payload. Set this to override the default `content-type`
                         header value.
    }
    public function setByteChannel(io:ByteChannel payload, string contentType = "application/octec-stream");

    documentation {
        Sets the request payload.

        P{{payload}} Payload can be of type `string`, `xml`, `json`, `blob`, `ByteChannel` or `Entity[]` (i.e: a set
                     of body parts)
    }
    public function setPayload ((string | xml | json | blob | io:ByteChannel | mime:Entity[]) payload);

    // For use within the package. Takes the Cache-Control header and parses it to a RequestCacheControl object.
    function parseCacheControlHeader();
};

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

public function Request::hasHeader(string headerName) returns (boolean) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.hasHeader(headerName);
}

public function Request::getHeader(string headerName) returns (string) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeader(headerName);
}

public function Request::getHeaders(string headerName) returns (string[]) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeaders(headerName);
}

public function Request::setHeader(string headerName, string headerValue) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setHeader(headerName, headerValue);
}

public function Request::addHeader(string headerName, string headerValue) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.addHeader(headerName, headerValue);
}

public function Request::removeHeader(string key) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.removeHeader(key);
}

public function Request::removeAllHeaders() {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.removeAllHeaders();
}

public function Request::getHeaderNames() returns (string[]) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeaderNames();
}

public function Request::expects100Continue() returns (boolean) {
    return self.hasHeader(EXPECT) ? self.getHeader(EXPECT) == "100-continue" : false;
}

public function Request::setContentType(string contentType) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setContentType(contentType);
}

public function Request::getContentType() returns (string) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getContentType();
}

public function Request::getJsonPayload() returns json|PayloadError {
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

public function Request::getXmlPayload() returns xml|PayloadError {
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

public function Request::getTextPayload() returns (string | PayloadError) {
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

public function Request::getPayloadAsString () returns (string | PayloadError) {
    match self.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getBodyAsString() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                string stringPayload => return stringPayload;
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

public function Request::getByteChannel() returns io:ByteChannel|PayloadError {
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

public function Request::getFormParams() returns (map<string>|PayloadError) {
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
                                string name = entries[entryIndex].substring(0, index).trim();
                                int size = entries[entryIndex].length();
                                string value = entries[entryIndex].substring(index + 1, size).trim();
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

public function Request::getBodyParts() returns mime:Entity[]|mime:EntityError {
    var mimeEntity = self.getEntity();
    match mimeEntity {
        mime:Entity entity => return entity.getBodyParts();
        mime:EntityError err => return err;
    }
}

public function Request::setJsonPayload(json payload, string contentType = "application/json") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setJson(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setXmlPayload(xml payload, string contentType = "application/xml") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setXml(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setTextPayload(string payload, string contentType="text/plain") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setText(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setBinaryPayload(blob payload, string contentType = "application/octet-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBlob(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setBodyParts(mime:Entity[] bodyParts, string contentType = "multipart/form-data") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBodyParts(bodyParts, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setFileAsPayload(string filePath, @sensitive string contentType = "application/octet-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setFileAsEntityBody(filePath, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setByteChannel(io:ByteChannel payload, string contentType = "application/octet-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setByteChannel(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Request::setPayload(string|xml|json|blob|io:ByteChannel|mime:Entity[] payload) {
    match payload {
        string textContent => self.setTextPayload(textContent);
        xml xmlContent => self.setXmlPayload(xmlContent);
        json jsonContent => self.setJsonPayload(jsonContent);
        blob blobContent => self.setBinaryPayload(blobContent);
        io:ByteChannel byteChannelContent => self.setByteChannel(byteChannelContent);
        mime:Entity[] bodyParts => self.setBodyParts(bodyParts);
    }
}


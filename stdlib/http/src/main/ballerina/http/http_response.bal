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
import ballerina/crypto;
import ballerina/time;

documentation {
    Represents an HTTP response.

    F{{statusCode}} The response status code
    F{{reasonPhrase}} The status code reason phrase
    F{{server}} The server header
    F{{resolvedRequestedURI}} The ultimate request URI that was made to receive the response when redirect is on
    F{{cacheControl}} The cache-control directives for the response. This needs to be explicitly initialized if
                      intending on utilizing HTTP caching. For incoming responses, this will already be populated
                      if the response was sent with cache-control directives
}
public type Response object {

    public int statusCode = 200;
    public string reasonPhrase;
    public string server;
    public string resolvedRequestedURI;
    public ResponseCacheControl? cacheControl;

    int receivedTime;
    int requestTime;
    private mime:Entity entity;

    public new() {
        self.entity = createNewEntity();
    }

    documentation {
        Create a new `Entity` and link it with the response.

        R{{}} Newly created `Entity` that has been set to the response
    }
    extern function createNewEntity() returns mime:Entity;

    documentation {
        Gets the `Entity` associated with the response.

        R{{}} The `Entity` of the response. An `error` is returned, if entity construction fails
    }
    public extern function getEntity() returns mime:Entity|error;

    //Gets the `Entity` from the response without the entity body. This function is exposed only to be used internally.
    extern function getEntityWithoutBody() returns mime:Entity;

    documentation {
        Sets the provided `Entity` to the response.

        P{{e}} The `Entity` to be set to the response
    }
    public extern function setEntity(mime:Entity e);

    documentation {
        Checks whether the requested header key exists in the header map.

        P{{headerName}} The header name
        R{{}} Returns true if the specified header key exists
    }
    public function hasHeader(string headerName) returns boolean;

    documentation {
        Returns the value of the specified header. If the specified header key maps to multiple values, the first of
        these values is returned.

        P{{headerName}} The header name
        R{{}} The first header value for the specified header name. An exception is thrown if no header is found. Use
              `hasHeader()` beforehand to check the existence of header.
    }
    public function getHeader(string headerName) returns string;

    documentation {
        Adds the specified header to the response. Existing header values are not replaced.

        P{{headerName}} The header name
        P{{headerValue}} The header value
    }
    public function addHeader(string headerName, string headerValue);

    documentation {
        Gets all the header values to which the specified header key maps to.

        P{{headerName}} The header name
        R{{}} The header values the specified header key maps to. An exception is thrown if no header is found. Use
              `hasHeader()` beforehand to check the existence of header.
    }
    public function getHeaders(string headerName) returns (string[]);

    documentation {
        Sets the specified header to the response. If a mapping already exists for the specified header key, the
        existing header value is replaced with the specfied header value.

        P{{headerName}} The header name
        P{{headerValue}} The header value
    }
    public function setHeader(string headerName, string headerValue);

    documentation {
        Removes the specified header from the response.

        P{{key}} The header name
    }
    public function removeHeader(string key);

    documentation {
        Removes all the headers from the response.
    }
    public function removeAllHeaders();

    documentation {
        Gets all the names of the headers of the response.

        R{{}} An array of all the header names
    }
    public function getHeaderNames() returns string[];

    documentation {
        Sets the `content-type` header to the response.

        P{{contentType}} Content type value to be set as the `content-type` header
    }
    public function setContentType(string contentType);

    documentation {
        Gets the type of the payload of the response (i.e: the `content-type` header value).

        R{{}} Returns the `content-type` header value as a string
    }
    public function getContentType() returns string;

    documentation {
        Extract `json` payload from the response. If the content type is not JSON, an `error` is returned.

        R{{}} The `json` payload or `error` in case of errors
    }
    public function getJsonPayload() returns json|error;

    documentation {
        Extracts `xml` payload from the response. If the the content type is not XML, an `error` is returned.

        R{{}} The `xml` payload or `error` in case of errors
    }
    public function getXmlPayload() returns xml|error;

    documentation {
        Extracts `text` payload from the response. If the content type is not of type text, an `error` is returned.

        R{{}} The string representation of the message payload or `error` in case of errors
    }
    public function getTextPayload() returns string|error;

    documentation {
        Gets the response payload as a `string`. Content type is not checked during payload construction which
        makes this different from `getTextPayload()` function.

        R{{}} The string representation of the message payload or `error` in case of errors
    }
    public function getPayloadAsString() returns string|error;

    documentation {
        Gets the response payload as a `ByteChannel`, except in the case of multiparts. To retrieve multiparts, use
        `getBodyParts()`.

        R{{}} A byte channel from which the message payload can be read or `error` in case of errors
    }
    public function getByteChannel() returns io:ByteChannel|error;

    documentation {
        Gets the response payload as a `byte[]`.

        R{{}} The byte[] representation of the message payload or `error` in case of errors
    }
    public function getBinaryPayload() returns byte[]|error;

    documentation {
        Extracts body parts from the response. If the content type is not a composite media type, an error is returned.

        R{{}} Returns the body parts as an array of entities or an `error` if there were any errors in
              constructing the body parts from the response
    }
    public function getBodyParts() returns mime:Entity[]|error;

    documentation {
        Sets the `etag` header for the given payload. The ETag is generated using a CRC32 hash function.

        P{{payload}} The payload for which the ETag should be set
    }
    public function setETag(json|xml|string|byte[] payload);

    documentation {
        Sets the current time as the `last-modified` header.
    }
    public function setLastModified();

    documentation {
        Sets a `json` as the payload.

        P{{payload}} The `json` payload
        P{{contentType}} The content type of the payload. Set this to override the default `content-type` header value
                         for `json`
    }
    public function setJsonPayload(json payload, string contentType = "application/json");

    documentation {
        Sets an `xml` as the payload

        P{{payload}} The `xml` payload
        P{{contentType}} The content type of the payload. Set this to override the default `content-type` header value
                         for `xml`
    }
    public function setXmlPayload(xml payload, string contentType = "application/xml");

    documentation {
        Sets a `string` as the payload.

        P{{payload}} The `string` payload
        P{{contentType}} The content type of the payload. Set this to override the default `content-type` header value
                         for `string`
    }
    public function setTextPayload(string payload, string contentType = "text/plain");

    documentation {
        Sets a `byte[]` as the payload.

        P{{payload}} The `byte[]` payload
        P{{contentType}} The content type of the payload. Set this to override the default `content-type` header value
                         for `byte[]`
    }
    public function setBinaryPayload(byte[] payload, string contentType = "application/octet-stream");

    documentation {
        Set multiparts as the payload.

        P{{bodyParts}} The entities which make up the message body
        P{{contentType}} The content type of the top level message. Set this to override the default
                         `content-type` header value
    }
    public function setBodyParts(mime:Entity[] bodyParts, string contentType = "multipart/form-data");

    documentation {
        Sets the content of the specified file as the entity body of the response.

        P{{filePath}} Path to the file to be set as the payload
        P{{contentType}} The content type of the specified file. Set this to override the default `content-type`
                         header value
    }
    public function setFileAsPayload(string filePath, string contentType = "application/octet-stream");

    documentation {
        Sets a `ByteChannel` as the payload.

        P{{payload}} A `ByteChannel` through which the message payload can be read
        P{{contentType}} The content type of the payload. Set this to override the default `content-type`
                         header value
    }
    public function setByteChannel(io:ByteChannel payload, string contentType = "application/octet-stream");

    documentation {
        Sets the response payload.

        P{{payload}} Payload can be of type `string`, `xml`, `json`, `byte[]`, `ByteChannel` or `Entity[]` (i.e: a set
                     of body parts)
    }
    public function setPayload(string|xml|json|byte[]|io:ByteChannel|mime:Entity[] payload);
};

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

function Response::hasHeader(string headerName) returns boolean {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.hasHeader(headerName);
}

function Response::getHeader(string headerName) returns string {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeader(headerName);
}

function Response::addHeader(string headerName, string headerValue) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.addHeader(headerName, headerValue);
}

function Response::getHeaders(string headerName) returns (string[]) {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeaders(headerName);
}

function Response::setHeader(string headerName, string headerValue) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setHeader(headerName, headerValue);

    // TODO: see if this can be handled in a better manner
    if (SERVER.equalsIgnoreCase(headerName)) {
        self.server = headerValue;
    }
}

function Response::removeHeader(string key) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.removeHeader(key);
}

function Response::removeAllHeaders() {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.removeAllHeaders();
}

function Response::getHeaderNames() returns string[] {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getHeaderNames();
}

function Response::setContentType(string contentType) {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setHeader(mime:CONTENT_TYPE, contentType);
}

function Response::getContentType() returns string {
    mime:Entity entity = self.getEntityWithoutBody();
    return entity.getContentType();
}

function Response::getJsonPayload() returns json|error {
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

function Response::getXmlPayload() returns xml|error {
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

function Response::getTextPayload() returns string|error {
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

function Response::getPayloadAsString() returns string|error {
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

function Response::getBinaryPayload() returns byte[]|error {
    match self.getEntity() {
        error err => return err;
        mime:Entity mimeEntity => {
            match mimeEntity.getByteArray() {
                error payloadErr => return payloadErr;
                byte[] binaryPayload => return binaryPayload;
            }
        }
    }
}

function Response::getByteChannel() returns io:ByteChannel|error {
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

function Response::getBodyParts() returns mime:Entity[]|error {
    var mimeEntity = self.getEntity();
    match mimeEntity {
        mime:Entity entity => return entity.getBodyParts();
        error err => return err;
    }
}

function Response::setETag(json|xml|string|byte[] payload) {
    string etag = crypto:crc32(payload);
    self.setHeader(ETAG, etag);
}

function Response::setLastModified() {
    time:Time currentT = time:currentTime();
    string lastModified = currentT.format(time:TIME_FORMAT_RFC_1123);
    self.setHeader(LAST_MODIFIED, lastModified);
}

function Response::setJsonPayload(json payload, string contentType = "application/json") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setJson(payload, contentType = contentType);
    self.setEntity(entity);
}

function Response::setXmlPayload(xml payload, string contentType = "application/xml") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setXml(payload, contentType = contentType);
    self.setEntity(entity);
}

function Response::setTextPayload(string payload, string contentType = "text/plain") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setText(payload, contentType = contentType);
    self.setEntity(entity);
}

function Response::setBinaryPayload(byte[] payload, string contentType = "application/octet-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setByteArray(payload, contentType = contentType);
    self.setEntity(entity);
}

function Response::setBodyParts(mime:Entity[] bodyParts, string contentType = "multipart/form-data") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBodyParts(bodyParts, contentType = contentType);
    self.setEntity(entity);
}

function Response::setFileAsPayload(string filePath, string contentType = "application/octet-stream")
{
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setFileAsEntityBody(filePath, contentType = contentType);
    self.setEntity(entity);
}

function Response::setByteChannel(io:ByteChannel payload, string contentType = "application/octet-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setByteChannel(payload, contentType = contentType);
    self.setEntity(entity);
}

function Response::setPayload(string|xml|json|byte[]|io:ByteChannel|mime:Entity[] payload) {
    match payload {
        string textContent => self.setTextPayload(textContent);
        xml xmlContent => self.setXmlPayload(xmlContent);
        json jsonContent => self.setJsonPayload(jsonContent);
        byte[] blobContent => self.setBinaryPayload(blobContent);
        io:ByteChannel byteChannelContent => self.setByteChannel(byteChannelContent);
        mime:Entity[] bodyParts => self.setBodyParts(bodyParts);
    }
}

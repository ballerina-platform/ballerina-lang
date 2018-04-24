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
    F{{cacheControl}} The cache-control directives for the response. This needs to be explicitly initialized if
                      intending on utilizing HTTP caching. For incoming responses, this will already be populated
                      if the response was sent with cache-control directives.
}
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
        Gets the `Entity` associated with the response.

        R{{}} The `Entity` of the response. `EntityError` will be returned if entity construction fails.
    }
    public native function getEntity () returns mime:Entity|mime:EntityError;

    documentation {
        Gets the `Entity` from the response without the entity body.

        R{{}} The `Entity` of the response
    }
    public native function getEntityWithoutBody () returns (mime:Entity);

    documentation {
        Sets the provided `Entity` to the response.

        P{{entity}} The `Entity` to be set to the response
    }
    public native function setEntity (mime:Entity entity);

    documentation {
        Checks whether the requested header key exists in the header map.

        P{{headerName}} The header name
        R{{}} Returns true if the specified header key exists
    }
    public function hasHeader (string headerName) returns (boolean);

    documentation {
        Returns the value of the specified header. If the specified header key maps to multiple values, the first of
        these values is returned.

        P{{headerName}} The header name
        R{{}} The first header value for the specified header name. Returns an empty string if the header does not
              exist.
    }
    public function getHeader (string headerName) returns (string);

    documentation {
        Adds the specified header to the response. Existing header values are not replaced.

        P{{headerName}} The header name
        P{{headerValue}} The header value
    }
    public function addHeader (string headerName, string headerValue);

    documentation {
        Gets all the header values to which the specified header key maps to.

        P{{headerName}} The header name
        R{{}} The header values the specified header key maps to
    }
    public function getHeaders (string headerName) returns (string[]);

    documentation {
        Sets the specified header to the response. If a mapping already exists for the specified header key, the
        existing header value is replaced with the specfied header value.

        P{{headerName}} The header name
        P{{headerValue}} The header value
    }
    public function setHeader (string headerName, string headerValue);

    documentation {
        Removes the specified header from the response.

        P{{key}} The header name
    }
    public function removeHeader (string key);

    documentation {
        Removes all the headers from the response.
    }
    public function removeAllHeaders ();

    documentation {
        Gets all the names of the headers of the response.

        R{{}} An array of all the header names
    }
    public function getHeaderNames () returns (string[]);

    documentation {
        Sets the `content-type` header to the response.

        P{{contentType}} Content type value to be set as the `content-type` header
    }
    public function setContentType (string contentType);

    documentation {
        Gets the type of the payload of the response (i.e: the `content-type` header value).

        R{{}} Returns the `content-type` header value as a string
    }
    public function getContentType () returns (string);

    documentation {
        Gets the response payload as a `json`.

        R{{}} The JSON reresentation of the message payload or `PayloadError` in case of errors
    }
    public function getJsonPayload () returns json|PayloadError;

    documentation {
        Gets the response payload as an `xml`.

        R{{}} The XML representation of the message payload or `PayloadError` in case of errors
    }
    public function getXmlPayload () returns xml|PayloadError;

    @Description {value:"Get the text payload from the response"}
    @Param {value:"response: The response message"}
    @Return {value:"The string representation of the message payload or 'PayloadError' in case of errors"}
    public function getTextPayload () returns (string | PayloadError);

    documentation {
        Gets the response payload as a `string`.

        R{{}} The string representation of the message payload or `PayloadError` in case of errors
    }
    public function getPayloadAsString () returns (string | PayloadError);

    documentation {
        Gets the response payload as a `blob`.

        R{{}} The blob representation of the message payload or `PayloadError` in case of errors
    }
    public function getBinaryPayload () returns (blob | PayloadError);

    documentation {
        Gets the response payload as a `ByteChannel` except in the case of multiparts. To retrieve multiparts, use
        `getBodyParts()`.

        R{{}} A byte channel from which the message payload can be read or `PayloadError` in case of errors
    }
    public function getByteChannel () returns (io:ByteChannel | PayloadError);

    documentation {
        Get multiparts from response.

        R{{}} Returns the body parts as an array of entities or an `EntityError` if there were any errors in
              constructing the body parts from the response
    }
    public function getBodyParts () returns (mime:Entity[] | mime:EntityError);

    documentation {
        Sets the `etag` header for the given payload. The ETag is generated using a CRC32 hash function.

        P{{}} The payload for which the ETag should be set.
    }
    public function setETag(json|xml|string|blob payload);

    documentation {
        Sets the current time as the `last-modified` header.
    }
    public function setLastModified();

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
    public function setBinaryPayload(blob payload, string contentType = "application/octet-stream");

    documentation {
        Set multiparts as the payload.

        P{{bodyParts}} The entities which make up the message body
        P{{contentType}} The content type of the top level message. Set this to override the default
                         `content-type` header value.
    }
    public function setBodyParts(mime:Entity[] bodyParts, string contentType = "multipart/form-data");

    documentation {
        Sets the content of the specified file as the entity body of the response.

        P{{filePath}} Path to the file to be set as the payload
        P{{contentType}} The content type of the specified file. Set this to override the default `content-type`
                         header value.
    }
    public function setFileAsPayload (string filePath, string contentType = "application/octet-stream");

    documentation {
        Sets a `ByteChannel` as the payload.

        P{{payload}} A `ByteChannel` through which the message payload can be read
        P{{contentType}} The content type of the payload. Set this to override the default `content-type`
                         header value.
    }
    public function setByteChannel(io:ByteChannel payload, string contentType = "application/octet-stream");

    documentation {
        Sets the response payload.

        P{{payload}} Payload can be of type `string`, `xml`, `json`, `blob`, `ByteChannel` or `Entity[]` (i.e: a set
                     of body parts)
    }
    public function setPayload (string|xml|json|blob|io:ByteChannel|mime:Entity[] payload);
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

public function Response::getJsonPayload () returns json|PayloadError {
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

public function Response::getXmlPayload () returns xml|PayloadError {
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

public function Response::getTextPayload() returns (string | PayloadError) {
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

public function Response::getPayloadAsString() returns (string | PayloadError) {
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

public function Response::getBinaryPayload () returns blob|PayloadError {
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

public function Response::getByteChannel () returns io:ByteChannel|PayloadError {
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

public function Response::getBodyParts () returns mime:Entity[]|mime:EntityError {
    var mimeEntity = self.getEntity();
    match mimeEntity {
        mime:Entity entity => return entity.getBodyParts();
        mime:EntityError err => return err;
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

public function Response::setTextPayload(string payload, string contentType="text/plain") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setText(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setBinaryPayload(blob payload, string contentType="application/octet-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBlob(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setBodyParts (mime:Entity[] bodyParts, string contentType="multipart/form-data") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setBodyParts(bodyParts, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setFileAsPayload (string filePath, @sensitive string contentType = "application/octet-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setFileAsEntityBody(filePath, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setByteChannel (io:ByteChannel payload, string contentType="application/octet-stream") {
    mime:Entity entity = self.getEntityWithoutBody();
    entity.setByteChannel(payload, contentType = contentType);
    self.setEntity(entity);
}

public function Response::setPayload (string|xml|json|blob|io:ByteChannel|mime:Entity[] payload) {
    match payload {
        string textContent => self.setTextPayload(textContent);
        xml xmlContent => self.setXmlPayload(xmlContent);
        json jsonContent => self.setJsonPayload(jsonContent);
        blob blobContent => self.setBinaryPayload(blobContent);
        io:ByteChannel byteChannelContent => self.setByteChannel(byteChannelContent);
        mime:Entity[] bodyParts => self.setBodyParts(bodyParts);
    }
}


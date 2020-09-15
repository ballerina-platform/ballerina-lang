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

import ballerina/io;
import ballerina/mime;
import ballerina/crypto;
import ballerina/time;
import ballerina/stringutils;
import ballerina/java;
import ballerina/log;

# Represents an HTTP response.
#
# + statusCode - The response status code
# + reasonPhrase - The status code reason phrase
# + server - The server header
# + resolvedRequestedURI - The ultimate request URI that was made to receive the response when redirect is on
# + cacheControl - The cache-control directives for the response. This needs to be explicitly initialized if
#                  intending on utilizing HTTP caching. For incoming responses, this will already be populated
#                  if the response was sent with cache-control directives
public class Response {

    public int statusCode = 200;
    public string reasonPhrase = "";
    public string server = "";
    public string resolvedRequestedURI = "";
    public ResponseCacheControl? cacheControl = ();

    int receivedTime = 0;
    int requestTime = 0;
    private mime:Entity? entity = ();

    public function init() {
        self.entity = self.createNewEntity();
    }

    # Create a new `Entity` and link it with the response.
    #
    # + return - Newly created `Entity` that has been set to the response
    function createNewEntity() returns mime:Entity {
        return externCreateNewResEntity(self);
    }

    # Gets the `Entity` associated with the response.
    #
    # + return - The `Entity` of the response. An `http:ClientError` is returned, if entity construction fails
    public function getEntity() returns mime:Entity|ClientError {
        return externGetResEntity(self);
    }

    // Gets the `Entity` from the response without the entity body. This function is exposed only to be used
    // internally.
    function getEntityWithoutBodyAndHeaders() returns mime:Entity {
        return externGetResEntityWithoutBodyAndHeaders(self);
    }

    // Gets the `Entity` from the response with the body, but without headers. This function is used for Http level
    // functions.
    function getEntityWithBodyAndWithoutHeaders() returns mime:Entity|ClientError {
        return externGetResEntityWithBodyAndWithoutHeaders(self);
    }

    # Sets the provided `Entity` to the response.
    #
    # + e - The `Entity` to be set to the response
    public function setEntity(mime:Entity e) {
        return externSetResEntity(self, e);
    }

    # Sets the provided `Entity` to the request and update only the content type header in the transport message.
    #
    # + e - The `Entity` to be set to the request
    function setEntityAndUpdateContentTypeHeader(mime:Entity e) {
        return externSetResEntityAndUpdateContentTypeHeader(self, e);
    }

    # Checks whether the requested header key exists in the header map.
    #
    # + headerName - The header name
    # + position - Represents the position of the header as an optional parameter
    # + return - `true` if the specified header key exists
    public function hasHeader(string headerName, HeaderPosition position = LEADING) returns boolean {
        return externResponseHasHeader(self, headerName, position);
    }

    # Returns the value of the specified header. If the specified header key maps to multiple values, the first of
    # these values is returned.
    #
    # + headerName - The header name
    # + position - Represents the position of the header as an optional parameter. If the position is `http:TRAILING`,
    #              the entity-body of the `Response` must be accessed initially.
    # + return - The first header value for the specified header name. Panic if the header is not found. Use the
    #            `Response.hasHeader()` beforehand to check the existence of a header.
    public function getHeader(string headerName, HeaderPosition position = LEADING) returns @tainted string {
        return externResponseGetHeader(self, headerName, position);
    }

    # Adds the specified header to the response. Existing header values are not replaced. Panic if an illegal header is passed.
    #
    # + headerName - The header name
    # + headerValue - The header value
    # + position - Represents the position of the header as an optional parameter. If the position is `http:TRAILING`,
    #              the entity-body of the `Response` must be accessed initially.
    public function addHeader(string headerName, string headerValue, HeaderPosition position = LEADING) {
        return externResponseAddHeader(self, headerName, headerValue, position);
    }

    # Gets all the header values to which the specified header key maps to.
    #
    # + headerName - The header name
    # + position - Represents the position of the header as an optional parameter. If the position is `http:TRAILING`,
    #              the entity-body of the `Response` must be accessed initially.
    # + return - The header values the specified header key maps to. Panic if the header is not found. Use the
    #            `Response.hasHeader()` beforehand to check the existence of a header.
    public function getHeaders(string headerName, HeaderPosition position = LEADING) returns @tainted string[] {
        return externResponseGetHeaders(self, headerName, position);
    }

    # Sets the specified header to the response. If a mapping already exists for the specified header key, the
    # existing header value is replaced with the specified header value. Panic if an illegal header is passed.
    #
    # + headerName - The header name
    # + headerValue - The header value
    # + position - Represents the position of the header as an optional parameter. If the position is `http:TRAILING`,
    #              the entity-body of the `Response` must be accessed initially.
    public function setHeader(string headerName, string headerValue, HeaderPosition position = LEADING) {
        externResponseSetHeader(self, headerName, headerValue, position);


        // TODO: see if this can be handled in a better manner
        if (stringutils:equalsIgnoreCase(SERVER, headerName)) {
            self.server = headerValue;
        }
    }

    # Removes the specified header from the response.
    #
    # + headerName - The header name
    # + position - Represents the position of the header as an optional parameter. If the position is `http:TRAILING`,
    #              the entity-body of the `Response` must be accessed initially.
    public function removeHeader(string headerName, HeaderPosition position = LEADING) {
        externResponseRemoveHeader(self, headerName, position);
    }

    # Removes all the headers from the response.
    #
    # + position - Represents the position of the header as an optional parameter. If the position is `http:TRAILING`,
    #              the entity-body of the `Response` must be accessed initially.
    public function removeAllHeaders(HeaderPosition position = LEADING) {
        externResponseRemoveAllHeaders(self, position);
    }

    # Gets all the names of the headers of the response.
    #
    # + position - Represents the position of the header as an optional parameter. If the position is `http:TRAILING`,
    #              the entity-body of the `Response` must be accessed initially.
    # + return - An array of all the header names
    public function getHeaderNames(HeaderPosition position = LEADING) returns @tainted string[] {
        return externResponseGetHeaderNames(self, position);
    }

    # Sets the `content-type` header to the response.
    #
    # + contentType - Content type value to be set as the `content-type` header
    # + return - Nil if successful, error in case of invalid content-type
   public function setContentType(string contentType) returns error? {
        return trap self.setHeader(mime:CONTENT_TYPE, contentType);
    }

    # Gets the type of the payload of the response (i.e: the `content-type` header value).
    #
    # + return - Returns the `content-type` header value as a string
    public function getContentType() returns @tainted string {
        return self.getHeader(mime:CONTENT_TYPE);
    }

    # Extract `json` payload from the response. If the content type is not JSON, an `http:ClientError` is returned.
    #
    # + return - The `json` payload or `http:ClientError` in case of errors
    public function getJsonPayload() returns @tainted json|ClientError {
        var result = self.getEntityWithBodyAndWithoutHeaders();
        if (result is error) {
            return result;
        } else {
            var payload = externGetJson(result);
            if (payload is mime:Error) {
                if (payload.cause() is mime:NoContentError) {
                    return createErrorForNoPayload(payload);
                } else {
                    string message = "Error occurred while retrieving the json payload from the response";
                    return GenericClientError(message, payload);
               }
            } else {
                return payload;
            }
        }
    }

    # Extracts `xml` payload from the response.
    #
    # + return - The `xml` payload or `http:ClientError` in case of errors
    public function getXmlPayload() returns @tainted xml|ClientError {
        var result = self.getEntityWithBodyAndWithoutHeaders();
        if (result is error) {
            return result;
        } else {
            var payload = externGetXml(result);
            if (payload is mime:Error) {
                if (payload.cause() is mime:NoContentError) {
                    return createErrorForNoPayload(payload);
                } else {
                    string message = "Error occurred while retrieving the xml payload from the response";
                    return GenericClientError(message, payload);
               }
            } else {
                return payload;
            }
        }
    }

    # Extracts `text` payload from the response.
    #
    # + return - The string representation of the message payload or `http:ClientError` in case of errors
    public function getTextPayload() returns @tainted string|ClientError {
        var result = self.getEntityWithBodyAndWithoutHeaders();
        if (result is error) {
            return result;
        } else {
            var payload = externGetText(result);
            if (payload is mime:Error) {
                if (payload.cause() is mime:NoContentError) {
                    return createErrorForNoPayload(payload);
                } else {
                    string message = "Error occurred while retrieving the text payload from the response";
                    return GenericClientError(message, payload);
               }
            } else {
                return payload;
            }
        }
    }

    # Gets the response payload as a `ByteChannel`, except in the case of multiparts. To retrieve multiparts, use
    # `Response.getBodyParts()`.
    #
    # + return - A byte channel from which the message payload can be read or `http:ClientError` in case of errors
    public function getByteChannel() returns @tainted io:ReadableByteChannel|ClientError {
        var result = self.getEntityWithBodyAndWithoutHeaders();
        if (result is error) {
            return result;
        } else {
            var payload = externGetByteChannel(result);
            if (payload is mime:Error) {
                string message = "Error occurred while retrieving the byte channel from the response";
                return GenericClientError(message, payload);
            } else {
                return payload;
            }
        }
    }

    # Gets the response payload as a `byte[]`.
    #
    # + return - The byte[] representation of the message payload or `http:ClientError` in case of errors
    public function getBinaryPayload() returns @tainted byte[]|ClientError {
        var result = self.getEntityWithBodyAndWithoutHeaders();
        if (result is error) {
            return result;
        } else {
            var payload = externGetByteArray(result);
            if (payload is mime:Error) {
                string message = "Error occurred while retrieving the binary payload from the response";
                return GenericClientError(message, payload);
            } else {
                return payload;
            }
        }
    }

    # Extracts body parts from the response. If the content type is not a composite media type, an error is returned.
    #
    # + return - The body parts as an array of entities or else an `http:ClientError` if there were any errors in
    #            constructing the body parts from the response
    public function getBodyParts() returns mime:Entity[]|ClientError {
        var result = self.getEntity();
        if (result is ClientError) {
            // TODO: Confirm whether this is actually a ClientError or not.
            return result;
        } else {
            var bodyParts = result.getBodyParts();
            if (bodyParts is mime:Error) {
                string message = "Error occurred while retrieving body parts from the response";
                return GenericClientError(message, bodyParts);
            } else {
                return bodyParts;
            }
        }
    }

    # Sets the `etag` header for the given payload. The ETag is generated using a CRC32 hash function.
    #
    # + payload - The payload for which the ETag should be set
    public function setETag(json|xml|string|byte[] payload) {
        string etag = crypto:crc32b(payload.toString().toBytes());
        self.setHeader(ETAG, etag);
    }

    # Sets the current time as the `last-modified` header.
    public function setLastModified() {
        time:Time currentT = time:currentTime();
        var lastModified = time:format(currentT, time:TIME_FORMAT_RFC_1123);
        if (lastModified is string) {
            self.setHeader(LAST_MODIFIED, lastModified);
        } else {
            //This error is unlikely as the format is a constant and time is
            //the current time which  does not returns an error.
            panic lastModified;
        }
    }

    # Sets a `json` as the payload.
    #
    # + payload - The `json` payload
    # + contentType - The content type of the payload. Set this to override the default `content-type` header value
    #                 for `json`
    public function setJsonPayload(json payload, string contentType = "application/json") {
        mime:Entity entity = self.getEntityWithoutBodyAndHeaders();
        entity.setJson(payload, contentType);
        self.setEntityAndUpdateContentTypeHeader(entity);
    }

    # Sets an `xml` as the payload
    #
    # + payload - The `xml` payload
    # + contentType - The content type of the payload. Set this to override the default `content-type` header value
    #                 for `xml`
    public function setXmlPayload(xml payload, string contentType = "application/xml") {
        mime:Entity entity = self.getEntityWithoutBodyAndHeaders();
        entity.setXml(payload, contentType);
        self.setEntityAndUpdateContentTypeHeader(entity);
    }

    # Sets a `string` as the payload.
    #
    # + payload - The `string` payload
    # + contentType - The content type of the payload. Set this to override the default `content-type` header value
    #                 for `string`
    public function setTextPayload(string payload, string contentType = "text/plain") {
        mime:Entity entity = self.getEntityWithoutBodyAndHeaders();
        entity.setText(payload, contentType);
        self.setEntityAndUpdateContentTypeHeader(entity);
    }

    # Sets a `byte[]` as the payload.
    #
    # + payload - The `byte[]` payload
    # + contentType - The content type of the payload. Set this to override the default `content-type` header value
    #                 for `byte[]`
    public function setBinaryPayload(byte[] payload, string contentType = "application/octet-stream") {
        mime:Entity entity = self.getEntityWithoutBodyAndHeaders();
        entity.setByteArray(payload, contentType);
        self.setEntityAndUpdateContentTypeHeader(entity);
    }

    # Set multiparts as the payload.
    #
    # + bodyParts - The entities which make up the message body
    # + contentType - The content type of the top level message. Set this to override the default
    #                 `content-type` header value
    public function setBodyParts(mime:Entity[] bodyParts, string contentType = "multipart/form-data") {
        mime:Entity entity = self.getEntityWithoutBodyAndHeaders();
        entity.setBodyParts(bodyParts, contentType);
        self.setEntityAndUpdateContentTypeHeader(entity);
    }

    # Sets the content of the specified file as the entity body of the response.
    #
    # + filePath - Path to the file to be set as the payload
    # + contentType - The content type of the specified file. Set this to override the default `content-type`
    #                 header value
    public function setFileAsPayload(string filePath, string contentType = "application/octet-stream") {
        mime:Entity entity = self.getEntityWithoutBodyAndHeaders();
        entity.setFileAsEntityBody(filePath, contentType);
        self.setEntityAndUpdateContentTypeHeader(entity);
    }

    # Sets a `ByteChannel` as the payload.
    #
    # + payload - A `ByteChannel` through which the message payload can be read
    # + contentType - The content type of the payload. Set this to override the default `content-type`
    #                 header value
    public function setByteChannel(io:ReadableByteChannel payload, string contentType = "application/octet-stream") {
        mime:Entity entity = self.getEntityWithoutBodyAndHeaders();
        entity.setByteChannel(payload, contentType);
        self.setEntityAndUpdateContentTypeHeader(entity);
    }

    # Sets the response payload.
    #
    # + payload - Payload can be of type `string`, `xml`, `json`, `byte[]`, `ByteChannel` or `Entity[]` (i.e: a set
    #             of body parts)
    public function setPayload(string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[] payload) {
        if (payload is string) {
            self.setTextPayload(payload);
        } else if (payload is xml) {
            self.setXmlPayload(payload);
        } else if (payload is byte[]) {
            self.setBinaryPayload(payload);
        } else if (payload is json) {
            self.setJsonPayload(payload);
        } else if (payload is io:ReadableByteChannel) {
            self.setByteChannel(payload);
        } else {
            self.setBodyParts(payload);
        }
    }

    # Adds the cookie to response.
    #
    # + cookie - The cookie, which is added to response
    public function addCookie(Cookie cookie) {
        var result = cookie.isValid();
        if (result is boolean) {
            self.addHeader("Set-Cookie", cookie.toStringValue());
        } else {
            log:printError("Invalid Cookie", result);
        }
    }

    # Deletes the cookies in the client's cookie store.
    #
    # + cookiesToRemove - Cookies to be deleted
    public function removeCookiesFromRemoteStore(Cookie...cookiesToRemove) {
        foreach var cookie in cookiesToRemove {
            cookie.expires = "1994-03-12 08:12:22";
            cookie.maxAge = 0;
            self.addCookie(cookie);
        }
    }

    # Gets cookies from the response.
    #
    # + return - An array of cookie objects, which are included in the response
    public function getCookies() returns @tainted Cookie[] {
        Cookie[] cookiesInResponse = [];
        string[] cookiesStringValues = self.getHeaders("Set-Cookie");
        foreach string cookiesStringValue in cookiesStringValues {
            cookiesInResponse.push(parseSetCookieHeader(cookiesStringValue));
        }
        return cookiesInResponse;
    }
}

function externCreateNewResEntity(Response response) returns mime:Entity =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternResponse",
    name: "createNewEntity"
} external;

function externSetResEntity(Response response, mime:Entity entity) =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternResponse",
    name: "setEntity"
} external;

function externSetResEntityAndUpdateContentTypeHeader(Response response, mime:Entity entity) =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternResponse",
    name: "setEntityAndUpdateContentTypeHeader"
} external;

function externGetResEntity(Response response) returns mime:Entity|ClientError =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternResponse",
    name: "getEntity"
} external;

function externGetResEntityWithoutBodyAndHeaders(Response response) returns mime:Entity =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternResponse",
    name: "getEntityWithoutBodyAndHeaders"
} external;

function externGetResEntityWithBodyAndWithoutHeaders(Response response) returns mime:Entity =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternResponse",
    name: "getEntityWithBodyAndWithoutHeaders"
} external;

// HTTP header related external functions
function externResponseGetHeader(Response response, string headerName, HeaderPosition position)
                         returns @tainted string = @java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternHeaders",
    name: "getHeader"
} external;

function externResponseGetHeaders(Response response, string headerName, HeaderPosition position)
                          returns @tainted string[] = @java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternHeaders",
    name: "getHeaders"
} external;

function externResponseGetHeaderNames(Response response, HeaderPosition position) returns @tainted string[] =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternHeaders",
    name: "getHeaderNames"
} external;

function externResponseAddHeader(Response response, string headerName, string headerValue, HeaderPosition position) =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternHeaders",
    name: "addHeader"
} external;

function externResponseSetHeader(Response response, string headerName, string headerValue, HeaderPosition position) =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternHeaders",
    name: "setHeader"
} external;

function externResponseRemoveHeader(Response response, string headerName, HeaderPosition position) = @java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternHeaders",
    name: "removeHeader"
} external;

function externResponseRemoveAllHeaders(Response response, HeaderPosition position) = @java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternHeaders",
    name: "removeAllHeaders"
} external;

function externResponseHasHeader(Response response, string headerName, HeaderPosition position) returns boolean =
@java:Method {
    'class: "org.ballerinalang.net.http.nativeimpl.ExternHeaders",
    name: "hasHeader"
} external;

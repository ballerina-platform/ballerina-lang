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
import ballerina/lang.'int as langint;

# Key name for `boundary` parameter in MediaType. This is needed for composite type media types.
public const string BOUNDARY = "boundary";

# Key name for `start` parameter in MediaType. This determines which part in the multipart message contains the
# payload.
public const string START = "start";

# Key name for `type` parameter in MediaType. This indicates the MIME media type of the `root` body part.
public const string TYPE = "type";

# Key name for `charset` parameter in MediaType. This indicates the character set of the body text.
public const string CHARSET = "charset";

# Default charset to be used with MIME encoding and decoding.
public const string DEFAULT_CHARSET = "UTF-8";

# Permission to be used with opening a byte channel for overflow data.
const READ_PERMISSION = "r";

# Represents `content-id` header name.
public const string CONTENT_ID = "content-id";

# Represents `content-length` header name.
public const string CONTENT_LENGTH = "content-length";

# Represents `content-type` header name.
public const string CONTENT_TYPE = "content-type";

# Represents `content-disposition` header name.
public const string CONTENT_DISPOSITION = "content-disposition";

# Represents values in `Content-Disposition` header.
#
# + fileName - Default filename for storing the bodypart, if the receiving agent wishes to store it in an external
#              file
# + disposition - Indicates how the body part should be presented (inline, attachment or as form-data)
# + name - Represents the field name in case of `multipart/form-data`
# + parameters - A set of parameters, specified in attribute=value notation
public type ContentDisposition object {

    public string fileName = "";
    public string disposition = "";
    public string name = "";
    public map<string> parameters = {};

    # Converts the `ContentDisposition` type to a string suitable for use as the value of a corresponding MIME header.
    # + return - The `string` represnetation of the `ContentDisposition` object
    public function toString() returns string = external;
};

# Describes the nature of the data in the body of a MIME entity.
#
# + primaryType - Declares the general type of data
# + subType - A specific format of the primary type data
# + suffix - Identify the semantics of a specific media type
# + parameters - A set of parameters, specified in an attribute=value notation
public type MediaType object {

    public string primaryType = "";
    public string subType = "";
    public string suffix = "";
    public map<string> parameters = {};

    # Gets “primaryType/subtype+suffix” combination in string format.
    #
    # + return - Base type as a string from MediaType struct
    public function getBaseType() returns string {
        return self.primaryType + "/" + self.subType;
    }

    # Converts the media type to a string, suitable to be used as the value of a corresponding HTTP header.
    #
    # + return - Content type with parameters as a string
    public function toString() returns string {
        string contentType = self.getBaseType();
        // map<string> parameters = self.parameters;
        string[] arrKeys = self.parameters.keys();
        int size = arrKeys.length();
        if (size > 0) {
            contentType = contentType + "; ";
        }
        int index = 0;
        while (index < size) {
            string value = self.parameters[arrKeys[index]] ?: "";
            if (index == size - 1) {
                contentType = contentType + arrKeys[index] + "=" + value;
                break;
            } else {
                contentType = contentType + arrKeys[index] + "=" + value + ";";
                index = index + 1;
            }
        }
        return contentType;
    }
};

# Represents the headers and body of a message. This can be used to represent both the entity of a top level message
# and an entity(body part) inside of a multipart entity.
#
# + cType - Describes the data contained in the body of the entity
# + cId - Helps one body of an entity to make a reference to another
# + cLength - Represents the size of the entity
# + cDisposition - Represents values related to `Content-Disposition` header
public type Entity object {

    private MediaType cType;
    private string cId;
    private int cLength;
    private ContentDisposition cDisposition;

    # Sets the content-type to entity.
    #
    # + mediaType - Content type that needs to be set to the entity
    # + return - Nil if successful, error in case of invalid media-type
    public function setContentType(@untainted string mediaType) returns InvalidContentTypeError? {
        self.cType = check getMediaType(mediaType);
        self.setHeader(CONTENT_TYPE, mediaType);
    }

    # Gets the content type of entity.
    #
    # + return - Content type as a `string`
    public function getContentType() returns @tainted string {
        string contentTypeHeaderValue = "";
        if (self.hasHeader(CONTENT_TYPE)) {
            contentTypeHeaderValue = self.getHeader(CONTENT_TYPE);
        }
        return contentTypeHeaderValue;
    }

    # Sets the content ID of the entity.
    #
    # + contentId - Content ID that needs to be set to entity
    public function setContentId(@untainted string contentId) {
        self.cId = contentId;
        self.setHeader(CONTENT_ID, contentId);
    }

    # Gets the content ID of entity.
    #
    # + return - Content ID as a `string`
    public function getContentId() returns @tainted string {
        string contentId = "";
        if (self.hasHeader(CONTENT_ID)) {
            contentId = self.getHeader(CONTENT_ID);
        }
        return contentId;
    }

    # Sets the content length of the entity.
    #
    # + contentLength - Content length that needs to be set to entity
    public function setContentLength(@untainted int contentLength) {
        self.cLength = contentLength;
        var contentLengthStr = contentLength.toString();
        self.setHeader(CONTENT_LENGTH, contentLengthStr);
    }

    # Gets the content length of entity.
    #
    # + return - Content length as an `int`
    public function getContentLength() returns @tainted int|error {
        string contentLength = "";
        if (self.hasHeader(CONTENT_LENGTH)) {
            contentLength = self.getHeader(CONTENT_LENGTH);
        }
        if (contentLength == "") {
            return -1;
        } else {
            return langint:fromString(contentLength);
        }
    }

    # Sets the content disposition of the entity.
    #
    # + contentDisposition - Content disposition that needs to be set to entity
    public function setContentDisposition(ContentDisposition contentDisposition) {
        self.cDisposition = contentDisposition;
        self.setHeader(CONTENT_DISPOSITION, contentDisposition.toString());
    }

    # Gets the content disposition of entity.
    #
    # + return - A `ContentDisposition` object
    public function getContentDisposition() returns ContentDisposition {
        string contentDispositionVal = "";
        if (self.hasHeader(CONTENT_DISPOSITION)) {
            contentDispositionVal = self.getHeader(CONTENT_DISPOSITION);
        }
        return getContentDispositionObject(contentDispositionVal);
    }

    # Sets the body of the entity with the given content. Note that any string value is set as `text/plain`. To send a
    # JSON-compatible string, set the content-type header to `application/json` or use the `setJsonPayload` method instead.
    #
    # + entityBody - Entity body can be of the type `string`,`xml`,`json`,`byte[]`,`io:ReadableByteChannel`, or `Entity[]`.
    public function setBody(@untainted string|xml|json|byte[]|io:ReadableByteChannel|Entity[] entityBody) {
        if (entityBody is string) {
            self.setText(entityBody);
        } else if (entityBody is xml) {
            self.setXml(entityBody);
        } else if (entityBody is json) {
            self.setJson(entityBody);
        } else if (entityBody is byte[]) {
            self.setByteArray(entityBody);
        } else if(entityBody is io:ReadableByteChannel) {
            self.setByteChannel(entityBody);
        } else {
            self.setBodyParts(entityBody);
        }
    }

    # Sets the entity body with a given file. This method overrides any existing `content-type` headers
    # with the default content-type `application/octet-stream`. The default value `application/octet-stream`
    # can be overridden by passing the content type as an optional parameter.
    #
    # + filePath - Represents the path to the file
    # + contentType - Content type to be used with the payload. This is an optional parameter.
    #                 `application/octet-stream` is used as the default value.
    public function setFileAsEntityBody(@untainted string filePath, public string contentType = "application/octet-stream") {
        io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(filePath);
        self.setByteChannel(byteChannel, contentType = contentType);
    }

    # Sets the entity body with the given `json` content. This method overrides any existing `content-type` headers
    # with the default content-type `application/json`. The default value `application/json` can be overridden
    # by passing the content type as an optional parameter.
    #
    # + jsonContent - JSON content that needs to be set to entity
    # + contentType - Content type to be used with the payload. This is an optional parameter. `application/json`
    #                 is used as the default value.
    public function setJson(@untainted json jsonContent, @untainted public string contentType = "application/json") = external;

    # Extracts JSON body from the entity. If the entity body is not a JSON, an error is returned.
    #
    # + return - `json` data extracted from the the entity body. An `ParserError` record is returned in case of
    #            errors.
    public function getJson() returns @tainted json|ParserError = external;

    # Sets the entity body with the given XML content. This method overrides any existing content-type headers
    # with the default content-type `application/xml`. The default value `application/xml` can be overridden
    # by passing the content-type as an optional parameter.
    #
    # + xmlContent - XML content that needs to be set to entity
    # + contentType - Content type to be used with the payload. This is an optional parameter. `application/xml`
    #                 is used as the default value.
    public function setXml(@untainted xml xmlContent, @untainted public string contentType = "application/xml") = external;

    # Extracts `xml` body from the entity. If the entity body is not an XML, an error is returned.
    #
    # + return - `xml` data extracted from the the entity body. An `ParserError` record is returned in case of
    #            errors.
    public function getXml() returns @tainted xml|ParserError = external;

    # Sets the entity body with the given text content. This method overrides any existing content-type headers
    # with the default content-type `text/plain`. The default value `text/plain` can be overridden
    # by passing the content type as an optional parameter.
    #
    # + textContent - Text content that needs to be set to entity
    # + contentType - Content type to be used with the payload. This is an optional parameter. `text/plain`
    #                 is used as the default value.
    public function setText(@untainted string textContent, @untainted public string contentType = "text/plain") = external;

    # Extracts text body from the entity. If the entity body is not text compatible an error is returned.
    #
    # + return - `string` data extracted from the the entity body or `ParserError` in case of errors.
    public function getText() returns @tainted string|ParserError = external;

    # Sets the entity body with the given byte[] content. This method overrides any existing `content-type` headers
    # with the default content-type `application/octet-stream`. The default value `application/octet-stream`
    # can be overridden by passing the content type as an optional parameter.
    #
    # + blobContent - byte[] content that needs to be set to entity
    # + contentType - Content type to be used with the payload. This is an optional parameter.
    #                 `application/octet-stream` is used as the default value.
    public function setByteArray(@untainted byte[] blobContent, @untainted public string contentType =
                                                                            "application/octet-stream") = external;

    # Given an entity, gets the entity body as a `byte[]`. If the entity size is considerably large consider
    # using getByteChannel() method instead.
    #
    # + return - `byte[]` data extracted from the the entity body. An `ParserError` record is returned in case of
    #            errors.
    public function getByteArray() returns @tainted byte[]|ParserError = external;

    # Sets the entity body with the given byte channel content. This method overrides any existing content-type headers
    # with the default content-type `application/octet-stream`. The default value `application/octet-stream`
    # can be overridden by passing the content-type as an optional parameter.
    #
    # + byteChannel - Byte channel that needs to be set to entity
    # + contentType - Content-type to be used with the payload. This is an optional parameter.
    #                 `application/octet-stream` is used as the default value.
    public function setByteChannel(io:ReadableByteChannel byteChannel, @untainted public string contentType =
                                                                            "application/octet-stream") = external;

    # Given an entity, gets the entity body as a byte channel.
    #
    # + return - An `io:ReadableByteChannel`. An `ParserError` record will be returned in case of errors
    public function getByteChannel() returns @tainted io:ReadableByteChannel|ParserError = external;

    # Given an entity, gets its body parts. If the entity body is not a set of body parts an error will be returned.
    #
    # + return - An array of body parts(`Entity[]`) extracted from the entity body. An `ParserError` record will be
    #            returned in case of errors.
    public function getBodyParts() returns Entity[]|ParserError = external;

    # Given an entity, gets the body parts as a byte channel.
    #
    # + return - Body parts as a byte channel
    public function getBodyPartsAsChannel() returns @tainted io:ReadableByteChannel|ParserError = external;

    # Sets body parts to entity. This method overrides any existing `content-type` headers
    # with the default content-type `multipart/form-data`. The default value `multipart/form-data` can be overridden
    # by passing the content type as an optional parameter.
    #
    # + bodyParts - Represents the body parts that needs to be set to the entity
    # + contentType - Content-type to be used with the payload. This is an optional parameter.
    #                 `multipart/form-data` is used as the default value.
    public function setBodyParts(@untainted Entity[] bodyParts, @untainted public string contentType =
                                                                                "multipart/form-data") = external;

    # Gets the header value associated with the given header name.
    #
    # + headerName - Represents header name
    # + return - Header value associated with the given header name as a `string`. If multiple header values are
    #            present, then the first value is returned. An exception is thrown if no header is found. Use
    #            `Entity.hasHeader()` beforehand to check the existence of header.
    public function getHeader(@untainted string headerName) returns @tainted string = external;

    # Gets all the header values associated with the given header name.
    #
    # + headerName - The header name
    # + return - All the header values associated with the given header name as a `string[]`. An exception is thrown
    #            if no header is found. Use `Entity.hasHeader()` beforehand to check the existence of header.
    public function getHeaders(@untainted string headerName) returns @tainted string[] = external;

    # Gets all header names.
    #
    # + return - All header names as a `string[]`
    public function getHeaderNames() returns @tainted string[] = external;

    # Adds the given header value against the given header.
    #
    # + headerName - The header name
    # + headerValue - Represents the header value to be added
    public function addHeader(@untainted string headerName, string headerValue) = external;

    # Sets the given header value against the existing header. If a header already exists, its value is replaced
    # with the given header value.
    #
    # + headerName - The header name
    # + headerValue - Represents the header value
    public function setHeader(@untainted string headerName, string headerValue) = external;

    # Removes the given header from the entity.
    #
    # + headerName - Represents the header name
    public function removeHeader(@untainted string headerName) = external;

    # Removes all headers associated with the entity.
    public function removeAllHeaders() = external;

    # Checks whether the requested header key exists in the header map.
    #
    # + headerName - The header name
    # + return - True if the specified header key exists
    public function hasHeader(@untainted string headerName) returns boolean = external;
};

# **Deprecated API**. Encodes a given input with MIME specific Base64 encoding scheme.
#
# + contentToBeEncoded - Content that needs to be encoded can be of type `string`, `byte[]` or `io:ReadableByteChannel`
# + charset - Charset to be used. This is used only with the string input
# + return - If the given input is of type string, an encoded `string` is returned.
#            If the given input is of type byte[], an encoded `byte[]` is returned.
#            If the given input is of type io:ReadableByteChannel, an encoded `io:ReadableByteChannel` is returned.
#            In case of errors, an `EncodeError` record is returned.
public function base64Encode((string|byte[]|io:ReadableByteChannel) contentToBeEncoded, string charset = "utf-8")
    returns (string|byte[]|io:ReadableByteChannel|EncodeError) = external;

# **Deprecated API**. Decodes a given input with MIME specific Base64 encoding scheme.
#
# + contentToBeDecoded - Content that needs to be decoded can be of type `string`, `byte[]` or `io:ReadableByteChannel`
# + charset - Charset to be used. This is used only with the string input
# + return - If the given input is of type string, a decoded `string` is returned.
#            If the given input is of type byte[], a decoded `byte[]` is returned.
#            If the given input is of type io:ReadableByteChannel, a decoded `io:ReadableByteChannel` is returned.
#            In case of errors, an `DecodeError` record is returned.
public function base64Decode((string|byte[]|io:ReadableByteChannel) contentToBeDecoded, string charset = "utf-8")
    returns (string|byte[]|io:ReadableByteChannel|DecodeError) = external;

# **Deprecated API**. Encodes a given byte[] with Base64 encoding scheme.
#
# + valueToBeEncoded - Content that needs to be encoded
# + return - An encoded byte[]. In case of errors, an `EncodeError` record is returned
public function base64EncodeBlob(byte[] valueToBeEncoded) returns byte[]|EncodeError {
    var result = base64Encode(valueToBeEncoded);
    if (result is byte[]|EncodeError) {
        return result;
    } else {
        return prepareEncodingErrorWithDetail("Error occurred while encoding byte[]");
    }
}

# **Deprecated API**. Decodes a given byte[] with Base64 encoding scheme.
#
# + valueToBeDecoded - Content that needs to be decoded
# + return - A decoded `byte[]`. In case of errors, an `DecodeError` record is returned
public function base64DecodeBlob(byte[] valueToBeDecoded) returns byte[]|DecodeError {
    var result = base64Decode(valueToBeDecoded);
    if (result is byte[]|DecodeError) {
        return result;
    } else {
        return prepareDecodingErrorWithDetail("Error occurred while decoding byte[]");
    }
}

# Gets the encoding value from a given MediaType.
#
# + contentType - A MediaType struct
# + return - The encoding value as a `string`
function getEncoding(MediaType contentType) returns (string?) {
    return contentType.parameters[CHARSET];
}

# Given the Content-Type in string, gets the MediaType object populated with it.
#
# + contentType - Content-Type in string
# + return - `MediaType` object or an error in case of invalid content-type
public function getMediaType(string contentType) returns MediaType|InvalidContentTypeError = external;

# Given the Content-Disposition as a string, gets the ContentDisposition object with it.
#
# + contentDisposition - Content disposition string
# + return - A `ContentDisposition` object
public function getContentDispositionObject(string contentDisposition) returns ContentDisposition = external;



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

# Key name for `boundary` parameter in MediaType. This is needed for composite type media types.
@final public string BOUNDARY = "boundary";

# Key name for `start` parameter in MediaType. This determines which part in the multipart message contains the
# payload.
@final public string START = "start";

# Key name for `type` parameter in MediaType. This indicates the MIME media type of the `root` body part.
@final public string TYPE = "type";

# Key name for `charset` parameter in MediaType. This indicates the character set of the body text.
@final public string CHARSET = "charset";

# Default charset to be used with MIME encoding and decoding.
@final public string DEFAULT_CHARSET = "UTF-8";

# Permission to be used with opening a byte channel for overflow data.
@final io:Mode READ_PERMISSION = "r";

# Represents `content-id` header name.
@final public string CONTENT_ID = "content-id";

# Represents `content-length` header name.
@final public string CONTENT_LENGTH = "content-length";

# Represents `content-type` header name.
@final public string CONTENT_TYPE = "content-type";

# Represents `content-disposition` header name.
@final public string CONTENT_DISPOSITION = "content-disposition";

# Represents values in `Content-Disposition` header.
#
# + fileName - Default filename for storing the bodypart, if the receiving agent wishes to store it in an external
#              file
# + disposition - Indicates how the body part should be presented (inline, attachment or as form-data)
# + name - Represents the field name in case of `multipart/form-data`
# + parameters - A set of parameters, specified in attribute=value notation
public type ContentDisposition object {

    public string fileName;
    public string disposition;
    public string name;
    public map<string> parameters;

    # Converts the `ContentDisposition` type to a string suitable for use as the value of a corresponding MIME header.
    # + return - The `string` represnetation of the `ContentDisposition` object
    public extern function toString() returns (string);
};

# Describes the nature of the data in the body of a MIME entity.
#
# + primaryType - Declares the general type of data
# + subType - A specific format of the primary type data
# + suffix - Identify the semantics of a specific media type
# + parameters - A set of parameters, specified in an attribute=value notation
public type MediaType object {

    public string primaryType;
    public string subType;
    public string suffix;
    public map<string> parameters;

    # Gets “primaryType/subtype+suffix” combination in string format.
    #
    # + return - Base type as a `string` from MediaType struct
    public function getBaseType() returns (string);

    # Converts the media type to a `string`, suitable to be used as the value of a corresponding HTTP header.
    #
    # + return - Content type with parameters as a `string`
    public function toString() returns (string);
};

function MediaType::getBaseType() returns (string) {
    return self.primaryType + "/" + self.subType;
}

function MediaType::toString() returns (string) {
    string contentType = self.getBaseType();
    // map<string> parameters = self.parameters;
    string[] arrKeys = self.parameters.keys();
    int size = lengthof arrKeys;
    if (size > 0) {
        contentType = contentType + "; ";
    }
    int index = 0;
    while (index < size) {
        string value = self.parameters[arrKeys[index]] but { () => "" };
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
    public function setContentType(@sensitive string mediaType) {
        self.cType = check getMediaType(mediaType);
        self.setHeader(CONTENT_TYPE, mediaType);
    }

    # Gets the content type of entity.
    #
    # + return - Content type as a `string`
    public function getContentType() returns string {
        string contentTypeHeaderValue;
        if (self.hasHeader(CONTENT_TYPE)) {
            contentTypeHeaderValue = self.getHeader(CONTENT_TYPE);
        }
        return contentTypeHeaderValue;
    }

    # Sets the content ID of the entity.
    #
    # + contentId - Content ID that needs to be set to entity
    public function setContentId(@sensitive string contentId) {
        self.cId = contentId;
        self.setHeader(CONTENT_ID, contentId);
    }

    # Gets the content ID of entity.
    #
    # + return - Content ID as a `string`
    public function getContentId() returns string {
        string contentId;
        if (self.hasHeader(CONTENT_ID)) {
            contentId = self.getHeader(CONTENT_ID);
        }
        return contentId;
    }

    # Sets the content length of the entity.
    #
    # + contentLength - Content length that needs to be set to entity
    public function setContentLength(@sensitive int contentLength) {
        self.cLength = contentLength;
        var contentLengthStr = <string>contentLength;
        self.setHeader(CONTENT_LENGTH, contentLengthStr);
    }

    # Gets the content length of entity.
    #
    # + return - Content length as an `int`
    public function getContentLength() returns int|error {
        string contentLength;
        if (self.hasHeader(CONTENT_LENGTH)) {
            contentLength = self.getHeader(CONTENT_LENGTH);
        }
        if (contentLength == "") {
            return -1;
        } else {
            return <int>contentLength;
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
        string contentDispositionVal;
        if (self.hasHeader(CONTENT_DISPOSITION)) {
            contentDispositionVal = self.getHeader(CONTENT_DISPOSITION);
        }
        return getContentDispositionObject(contentDispositionVal);
    }

    # Sets the entity body with the given content.
    #
    # + entityBody - Entity body can be of type `string`,`xml`,`json`,`byte[]`,`io:ByteChannel` or `Entity[]`
    public function setBody(@sensitive string|xml|json|byte[]|io:ByteChannel|Entity[] entityBody);

    # Sets the entity body with a given file. This method overrides any existing `content-type` headers
    # with the default content type `application/octet-stream`. The default value `application/octet-stream`
    # can be overridden by passing the content type as an optional parameter.
    #
    # + filePath - Represents the path to the file
    # + contentType - Content type to be used with the payload. This is an optional parameter.
    #                 `application/octet-stream` is used as the default value.
    public function setFileAsEntityBody(@sensitive string filePath, string contentType = "application/octet-stream");

    # Sets the entity body with the given `json` content. This method overrides any existing `content-type` headers
    # with the default content type `application/json`. The default value `application/json` can be overridden
    # by passing the content type as an optional parameter.
    #
    # + jsonContent - JSON content that needs to be set to entity
    # + contentType - Content type to be used with the payload. This is an optional parameter. `application/json`
    #                 is used as the default value.
    public extern function setJson(@sensitive json jsonContent, @sensitive string contentType = "application/json");

    # Extracts JSON body from the entity. If the entity body is not a JSON, an error is returned.
    #
    # + return - `json` data extracted from the the entity body. An `error` record is returned in case of
    #            errors.
    public extern function getJson() returns @tainted json|error;

    # Sets the entity body with the given XML content. This method overrides any existing content-type headers
    # with the default content-type `application/xml`. The default value `application/xml` can be overridden
    # by passing the content-type as an optional parameter.
    #
    # + xmlContent - XML content that needs to be set to entity
    # + contentType - Content type to be used with the payload. This is an optional parameter. `application/xml`
    #                 is used as the default value.
    public extern function setXml(@sensitive xml xmlContent, @sensitive string contentType = "application/xml");

    # Extracts `xml` body from the entity. If the entity body is not an XML, an error is returned.
    #
    # + return - `xml` data extracted from the the entity body. An `error` record is returned in case of
    #            errors.
    public extern function getXml() returns @tainted xml|error;

    # Sets the entity body with the given text content. This method overrides any existing content-type headers
    # with the default content-type `text/plain`. The default value `text/plain` can be overridden
    # by passing the content type as an optional parameter.
    #
    # + textContent - Text content that needs to be set to entity
    # + contentType - Content type to be used with the payload. This is an optional parameter. `text/plain`
    #                 is used as the default value.
    public extern function setText(@sensitive string textContent, @sensitive string contentType = "text/plain");

    # Extracts text body from the entity. If the entity body is not text compatible an error is returned.
    #
    # + return - `string` data extracted from the the entity body or `error` in case of errors.
    public extern function getText() returns @tainted string|error;

    # Given an entity, gets the entity body as a string. Content type is checked during entity body construction which
    # makes this different from getText() method.
    #
    # + return - Entity body as a `string` or `error` in case of errors occurred during
    #            construction of the string body.

    public extern function getBodyAsString() returns @tainted string|error;

    # Sets the entity body with the given byte[] content. This method overrides any existing `content-type` headers
    # with the default content type `application/octet-stream`. The default value `application/octet-stream`
    # can be overridden by passing the content type as an optional parameter.
    #
    # + blobContent - byte[] content that needs to be set to entity
    # + contentType - Content type to be used with the payload. This is an optional parameter.
    #                 `application/octet-stream` is used as the default value.
    public extern function setByteArray(@sensitive byte[] blobContent, @sensitive string contentType = "application/octet-stream");

    # Given an entity, gets the entity body as a `byte[]`. If the entity size is considerably large consider
    # using getByteChannel() method instead.
    #
    # + return - `byte[]` data extracted from the the entity body. An `error` record is returned in case of
    #            errors.
    public extern function getByteArray() returns @tainted byte[]|error;

    # Sets the entity body with the given byte channel content. This method overrides any existing content-type headers
    # with the default content-type `application/octet-stream`. The default value `application/octet-stream`
    # can be overridden by passing the content-type as an optional parameter.
    #
    # + byteChannel - Byte channel that needs to be set to entity
    # + contentType - Content-type to be used with the payload. This is an optional parameter.
    #                 `application/octet-stream` is used as the default value.
    public extern function setByteChannel(io:ByteChannel byteChannel, @sensitive string contentType = "application/octet-stream");

    # Given an entity, gets the entity body as a byte channel.
    #
    # + return - An `io:ByteChannel`. An `error` record will be returned in case of errors
    public extern function getByteChannel() returns @tainted io:ByteChannel|error;

    # Given an entity, gets its body parts. If the entity body is not a set of body parts an error will be returned.
    #
    # + return - An array of body parts(`Entity[]`) extracted from the entity body. An `error` record will be
    #            returned in case of errors.
    public extern function getBodyParts() returns Entity[]|error;

    # Given an entity, gets the body parts as a byte channel.
    #
    # + return - Body parts as a byte channel
    public extern function getBodyPartsAsChannel() returns @tainted io:ByteChannel|error;

    # Sets body parts to entity. This method overrides any existing `content-type` headers
    # with the default content type `multipart/form-data`. The default value `multipart/form-data` can be overridden
    # by passing the content type as an optional parameter.
    #
    # + bodyParts - Represents the body parts that needs to be set to the entity
    # + contentType - Content-type to be used with the payload. This is an optional parameter.
    #                 `multipart/form-data` is used as the default value.
    public extern function setBodyParts(@sensitive Entity[] bodyParts, @sensitive string contentType = "multipart/form-data");

    # Gets the header value associated with the given header name.
    #
    # + headerName - Represents header name
    # + return - Header value associated with the given header name as a `string`. If multiple header values are
    #            present, then the first value is returned. An exception is thrown if no header is found. Use
    #            `hasHeader()` beforehand to check the existence of header.
    public extern function getHeader(@sensitive string headerName) returns @tainted string;

    # Gets all the header values associated with the given header name.
    #
    # + headerName - The header name
    # + return - All the header values associated with the given header name as a `string[]`. An exception is thrown
    #            if no header is found. Use `hasHeader()` beforehand to check the existence of header.
    public extern function getHeaders(@sensitive string headerName) returns @tainted string[];

    # Gets all header names.
    #
    # + return - All header names as a `string[]`
    public extern function getHeaderNames() returns @tainted string[];

    # Adds the given header value against the given header.
    #
    # + headerName - The header name
    # + headerValue - Represents the header value to be added
    public extern function addHeader(@sensitive string headerName, string headerValue);

    # Sets the given header value against the existing header. If a header already exists, its value is replaced
    # with the given header value.
    #
    # + headerName - The header name
    # + headerValue - Represents the header value
    public extern function setHeader(@sensitive string headerName, string headerValue);

    # Removes the given header from the entity.
    #
    # + headerName - Represents the header name
    public extern function removeHeader(@sensitive string headerName);

    # Removes all headers associated with the entity.
    public extern function removeAllHeaders();

    # Checks whether the requested header key exists in the header map.
    #
    # + headerName - The header name
    # + return - True if the specified header key exists
    public extern function hasHeader(@sensitive string headerName) returns boolean;
};

function Entity::setFileAsEntityBody(@sensitive string filePath,
                                            @sensitive string contentType = "application/octet-stream") {
    io:ByteChannel byteChannel = io:openFile(filePath, READ_PERMISSION);
    self.setByteChannel(byteChannel, contentType = contentType);
}

function Entity::setBody(@sensitive (string|xml|json|byte[]|io:ByteChannel|Entity[]) entityBody) {
    match entityBody {
        string textContent => self.setText(textContent);
        xml xmlContent => self.setXml(xmlContent);
        json jsonContent => self.setJson(jsonContent);
        byte[] blobContent => self.setByteArray(blobContent);
        io:ByteChannel byteChannelContent => self.setByteChannel(byteChannelContent);
        Entity[] bodyParts => self.setBodyParts(bodyParts);
    }
}

# Encodes a given input with MIME specific Base64 encoding scheme.
#
# + contentToBeEncoded - Content that needs to be encoded can be of type `string`, `byte[]` or `io ByteChannel`
# + charset - Charset to be used. This is used only with the string input
# + return - If the given input is of type string, an encoded `string` is returned.
#            If the given input is of type byte[], an encoded `byte[]` is returned.
#            If the given input is of type io:ByteChannel, an encoded `io:ByteChannel` is returned.
#            In case of errors, an `error` record is returned.
extern function base64Encode((string|byte[]|io:ByteChannel) contentToBeEncoded, string charset = "utf-8")
    returns (string|byte[]|io:ByteChannel|error);

# Decodes a given input with MIME specific Base64 encoding scheme.
#
# + contentToBeDecoded - Content that needs to be decoded can be of type `string`, `byte[]` or `io ByteChannel`
# + charset - Charset to be used. This is used only with the string input
# + return - If the given input is of type string, a decoded `string` is returned.
#            If the given input is of type byte[], a decoded `byte[]` is returned.
#            If the given input is of type io:ByteChannel, a decoded `io:ByteChannel` is returned.
#            In case of errors, an `error` record is returned.
extern function base64Decode((string|byte[]|io:ByteChannel) contentToBeDecoded, string charset = "utf-8")
    returns (string|byte[]|io:ByteChannel|error);

# Encodes a given byte[] with Base64 encoding scheme.
#
# + valueToBeEncoded - Content that needs to be encoded
# + return - An encoded byte[]. In case of errors, an `error` record is returned
public function base64EncodeBlob(byte[] valueToBeEncoded) returns byte[]|error {
    error customErr = {message:"Error occurred while encoding byte[]"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        byte[] returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        error encodeErr => return encodeErr;
    }
}

# Encodes a given string with Base64 encoding scheme.
#
# + valueToBeEncoded - Content that needs to be encoded
# + charset - Charset to be used
# + return - An encoded `string`. In case of errors, an `error` record is returned
public function base64EncodeString(string valueToBeEncoded, string charset = "utf-8") returns string|error {
    error customErr = {message:"Error occurred while encoding string"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return returnString;
        byte[] returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        error encodeErr => return encodeErr;
    }
}

# Encodes a given ByteChannel with Base64 encoding scheme.
#
# + valueToBeEncoded - Content that needs to be encoded
# + return - An encoded `io:ByteChannel`. In case of errors, an `error` record is returned
public function base64EncodeByteChannel(io:ByteChannel valueToBeEncoded) returns io:ByteChannel|error {
    error customErr = {message:"Error occurred while encoding ByteChannel content"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        byte[] returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        error encodeErr => return encodeErr;
    }
}

# Decodes a given byte[] with Base64 encoding scheme.
#
# + valueToBeDecoded - Content that needs to be decoded
# + return - A decoded `byte[]`. In case of errors, an `error` record is returned
public function base64DecodeBlob(byte[] valueToBeDecoded) returns byte[]|error {
    error customErr = {message:"Error occurred while decoding byte[]"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        byte[] returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        error decodeErr => return decodeErr;
    }
}

# Decodes a given string with Base64 encoding scheme.
#
# + valueToBeDecoded - Content that needs to be decoded
# + charset - Charset to be used
# + return - A decoded `string`. In case of errors, an `error` record is returned
public function base64DecodeString(string valueToBeDecoded, string charset = "utf-8") returns string|error {
    error customErr = {message:"Error occurred while decoding string"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return returnString;
        byte[] returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        error decodeErr => return decodeErr;
    }
}

# Decodes a given ByteChannel with Base64 encoding scheme.
#
# + valueToBeDecoded - Content that needs to be decoded
# + return - A decoded `io:ByteChannel`. In case of errors, an `error` record is returned
public function base64DecodeByteChannel(io:ByteChannel valueToBeDecoded) returns io:ByteChannel|error {
    error customErr = {message:"Error occurred while decoding ByteChannel content"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        byte[] returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        error decodeErr => return decodeErr;
    }
}

# Gets the encoding value from a given MediaType.
#
# + contentType - A MediaType struct
# + return - The encoding value as a `string`
function getEncoding(MediaType contentType) returns (string) {
    return contentType.parameters.CHARSET;
}

# Given the Content-Type in string, gets the MediaType object populated with it.
#
# + contentType - Content-Type in string
# + return - `MediaType` object or an error in case of error
public extern function getMediaType(string contentType) returns MediaType|error;

# Given the Content-Disposition as a string, gets the ContentDisposition object with it.
#
# + contentDisposition - Content disposition string
# + return - A `ContentDisposition` object
public extern function getContentDispositionObject(string contentDisposition) returns ContentDisposition;

# Converts given byte[] to a string.
#
# + encoding - Encoding to used in byte[] conversion to string
# + return - String representation of the given byte[]
public extern function byteArrayToString(byte[] b, string encoding) returns string;

# Encode a given byte[] with Base64 encoding scheme.
#
# + return - Return an encoded byte[]
public extern function base64EncodeByteArray(byte[] b) returns byte[];

# Decode a given byte[] with Base64 encoding scheme.
#
# + return - Return a decoded byte[]
public extern function base64DecodeByteArray(byte[] b) returns byte[];

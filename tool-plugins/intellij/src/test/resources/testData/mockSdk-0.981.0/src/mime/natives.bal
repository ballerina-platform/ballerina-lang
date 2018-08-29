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

documentation {
    Key name for `boundary` parameter in MediaType. This is needed for composite type media types.
}
@final public string BOUNDARY = "boundary";

documentation {
    Key name for `start` parameter in MediaType. This determines which part in the multipart message contains the
    payload.
}
@final public string START = "start";

documentation {
    Key name for `type` parameter in MediaType. This indicates the MIME media type of the `root` body part.
}
@final public string TYPE = "type";

documentation {
    Key name for `charset` parameter in MediaType. This indicates the character set of the body text.
}
@final public string CHARSET = "charset";

documentation {
    Default charset to be used with MIME encoding and decoding.
}
@final public string DEFAULT_CHARSET = "UTF-8";

documentation {
    Permission to be used with opening a byte channel for overflow data.
}
@final io:Mode READ_PERMISSION = "r";

documentation {
    Represents `content-id` header name.
}
@final public string CONTENT_ID = "content-id";

documentation {
    Represents `content-length` header name.
}
@final public string CONTENT_LENGTH = "content-length";

documentation {
    Represents `content-type` header name.
}
@final public string CONTENT_TYPE = "content-type";

documentation {
    Represents `content-disposition` header name.
}
@final public string CONTENT_DISPOSITION = "content-disposition";

documentation {
    Represents values in `Content-Disposition` header.

    F{{fileName}} Default filename for storing the bodypart, if the receiving agent wishes to store it in an external
                  file
    F{{disposition}} Indicates how the body part should be presented (inline, attachment or as form-data)
    F{{name}} Represents the field name in case of `multipart/form-data`
    F{{parameters}} A set of parameters, specified in attribute=value notation
}
public type ContentDisposition object {

    public string fileName;
    public string disposition;
    public string name;
    public map<string> parameters;

    documentation {
        Converts the `ContentDisposition` type to a string suitable for use as the value of a corresponding MIME header.
        R{{}} The `string` represnetation of the `ContentDisposition` object
    }
    public extern function toString() returns (string);
};

documentation {
    Describes the nature of the data in the body of a MIME entity.

    F{{primaryType}} Declares the general type of data
    F{{subType}} A specific format of the primary type data
    F{{suffix}} Identify the semantics of a specific media type
    F{{parameters}} A set of parameters, specified in an attribute=value notation
}
public type MediaType object {

    public string primaryType;
    public string subType;
    public string suffix;
    public map<string> parameters;

    documentation {
        Gets “primaryType/subtype+suffix” combination in string format.

        R{{}} Base type as a `string` from MediaType struct
    }
    public function getBaseType() returns (string);

    documentation {
        Converts the media type to a `string`, suitable to be used as the value of a corresponding HTTP header.
        R{{}} Content type with parameters as a `string`
    }
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

documentation {
    Represents the headers and body of a message. This can be used to represent both the entity of a top level message
    and an entity(body part) inside of a multipart entity.

    F{{cType}} Describes the data contained in the body of the entity
    F{{cId}} Helps one body of an entity to make a reference to another
    F{{cLength}} Represents the size of the entity
    F{{cDisposition}} Represents values related to `Content-Disposition` header
}
public type Entity object {

    private MediaType cType;
    private string cId;
    private int cLength;
    private ContentDisposition cDisposition;

    documentation {
        Sets the content-type to entity.

        P{{mediaType}} Content type that needs to be set to the entity
    }
    public function setContentType(@sensitive string mediaType) {
        self.cType = check getMediaType(mediaType);
        self.setHeader(CONTENT_TYPE, mediaType);
    }

    documentation {
        Gets the content type of entity.

        R{{}} Content type as a `string`
    }
    public function getContentType() returns string {
        string contentTypeHeaderValue;
        if (self.hasHeader(CONTENT_TYPE)) {
            contentTypeHeaderValue = self.getHeader(CONTENT_TYPE);
        }
        return contentTypeHeaderValue;
    }

    documentation {
        Sets the content ID of the entity.

        P{{contentId}} Content ID that needs to be set to entity
    }
    public function setContentId(@sensitive string contentId) {
        self.cId = contentId;
        self.setHeader(CONTENT_ID, contentId);
    }

    documentation {
        Gets the content ID of entity.

        R{{}} Content ID as a `string`
    }
    public function getContentId() returns string {
        string contentId;
        if (self.hasHeader(CONTENT_ID)) {
            contentId = self.getHeader(CONTENT_ID);
        }
        return contentId;
    }

    documentation {
        Sets the content length of the entity.

        P{{contentLength}} Content length that needs to be set to entity
    }
    public function setContentLength(@sensitive int contentLength) {
        self.cLength = contentLength;
        var contentLengthStr = <string>contentLength;
        self.setHeader(CONTENT_LENGTH, contentLengthStr);
    }

    documentation {
        Gets the content length of entity.

        R{{}} Content length as an `int`
    }
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

    documentation {
        Sets the content disposition of the entity.

        P{{contentDisposition}} Content disposition that needs to be set to entity
    }
    public function setContentDisposition(ContentDisposition contentDisposition) {
        self.cDisposition = contentDisposition;
        self.setHeader(CONTENT_DISPOSITION, contentDisposition.toString());
    }

    documentation {
        Gets the content disposition of entity.

        R{{}} A `ContentDisposition` object
    }
    public function getContentDisposition() returns ContentDisposition {
        string contentDispositionVal;
        if (self.hasHeader(CONTENT_DISPOSITION)) {
            contentDispositionVal = self.getHeader(CONTENT_DISPOSITION);
        }
        return getContentDispositionObject(contentDispositionVal);
    }

    documentation {
        Sets the entity body with the given content.

        P{{entityBody}} Entity body can be of type `string`,`xml`,`json`,`byte[]`,`io:ByteChannel` or `Entity[]`
    }
    public function setBody(@sensitive string|xml|json|byte[]|io:ByteChannel|Entity[] entityBody);

    documentation {
        Sets the entity body with a given file. This method overrides any existing `content-type` headers
        with the default content type `application/octet-stream`. The default value `application/octet-stream`
        can be overridden by passing the content type as an optional parameter.

        P{{filePath}} Represents the path to the file
        P{{contentType}} Content type to be used with the payload. This is an optional parameter.
                         `application/octet-stream` is used as the default value.
    }
    public function setFileAsEntityBody(@sensitive string filePath, string contentType = "application/octet-stream");

    documentation {
        Sets the entity body with the given `json` content. This method overrides any existing `content-type` headers
        with the default content type `application/json`. The default value `application/json` can be overridden
        by passing the content type as an optional parameter.

        P{{jsonContent}} JSON content that needs to be set to entity
        P{{contentType}} Content type to be used with the payload. This is an optional parameter. `application/json`
                         is used as the default value.
    }
    public extern function setJson(@sensitive json jsonContent, @sensitive string contentType = "application/json");

    documentation {
        Extracts JSON body from the entity. If the entity body is not a JSON, an error is returned.

        R{{}} `json` data extracted from the the entity body. An `error` record is returned in case of
              errors.
    }
    public extern function getJson() returns @tainted json|error;

    documentation {
        Sets the entity body with the given XML content. This method overrides any existing content-type headers
        with the default content-type `application/xml`. The default value `application/xml` can be overridden
        by passing the content-type as an optional parameter.

        P{{xmlContent}} XML content that needs to be set to entity
        P{{contentType}} Content type to be used with the payload. This is an optional parameter. `application/xml`
                         is used as the default value.
    }
    public extern function setXml(@sensitive xml xmlContent, @sensitive string contentType = "application/xml");

    documentation {
        Extracts `xml` body from the entity. If the entity body is not an XML, an error is returned.

        R{{}} `xml` data extracted from the the entity body. An `error` record is returned in case of
              errors.
    }
    public extern function getXml() returns @tainted xml|error;

    documentation {
        Sets the entity body with the given text content. This method overrides any existing content-type headers
        with the default content-type `text/plain`. The default value `text/plain` can be overridden
        by passing the content type as an optional parameter.

        P{{textContent}} Text content that needs to be set to entity
        P{{contentType}} Content type to be used with the payload. This is an optional parameter. `text/plain`
                         is used as the default value.
    }
    public extern function setText(@sensitive string textContent, @sensitive string contentType = "text/plain");

    documentation {
        Extracts text body from the entity. If the entity body is not text compatible an error is returned.

        R{{}} `string` data extracted from the the entity body or `error` in case of errors.
    }
    public extern function getText() returns @tainted string|error;

    documentation {
        Given an entity, gets the entity body as a string. Content type is checked during entity body construction which
        makes this different from getText() method.

        R{{}} Entity body as a `string` or `error` in case of errors occurred during
              construction of the string body.
    }
    public extern function getBodyAsString() returns @tainted string|error;

    documentation {
        Sets the entity body with the given byte[] content. This method overrides any existing `content-type` headers
        with the default content type `application/octet-stream`. The default value `application/octet-stream`
        can be overridden by passing the content type as an optional parameter.

        P{{blobContent}} byte[] content that needs to be set to entity
        P{{contentType}} Content type to be used with the payload. This is an optional parameter.
                         `application/octet-stream` is used as the default value.
    }
    public extern function setByteArray(@sensitive byte[] blobContent, @sensitive string contentType = "application/octet-stream");

    documentation {
        Given an entity, gets the entity body as a `byte[]`. If the entity size is considerably large consider
        using getByteChannel() method instead.

        R{{}} `byte[]` data extracted from the the entity body. An `error` record is returned in case of
              errors.
    }
    public extern function getByteArray() returns @tainted byte[]|error;

    documentation {
        Sets the entity body with the given byte channel content. This method overrides any existing content-type headers
        with the default content-type `application/octet-stream`. The default value `application/octet-stream`
        can be overridden by passing the content-type as an optional parameter.

        P{{byteChannel}} Byte channel that needs to be set to entity
        P{{contentType}} Content-type to be used with the payload. This is an optional parameter.
                         `application/octet-stream` is used as the default value.
    }
    public extern function setByteChannel(io:ByteChannel byteChannel, @sensitive string contentType = "application/octet-stream");

    documentation {
        Given an entity, gets the entity body as a byte channel.

        R{{}} An `io:ByteChannel`. An `error` record will be returned in case of errors
    }
    public extern function getByteChannel() returns @tainted io:ByteChannel|error;

    documentation {
        Given an entity, gets its body parts. If the entity body is not a set of body parts an error will be returned.

        R{{}} An array of body parts(`Entity[]`) extracted from the entity body. An `error` record will be
              returned in case of errors.
    }
    public extern function getBodyParts() returns Entity[]|error;

    documentation {
        Given an entity, gets the body parts as a byte channel.

        R{{}} Body parts as a byte channel
    }
    public extern function getBodyPartsAsChannel() returns @tainted io:ByteChannel;

    documentation {
        Sets body parts to entity. This method overrides any existing `content-type` headers
        with the default content type `multipart/form-data`. The default value `multipart/form-data` can be overridden
        by passing the content type as an optional parameter.

        P{{bodyParts}} Represents the body parts that needs to be set to the entity
        P{{contentType}} Content-type to be used with the payload. This is an optional parameter.
                         `multipart/form-data` is used as the default value.
    }
    public extern function setBodyParts(@sensitive Entity[] bodyParts, @sensitive string contentType = "multipart/form-data");

    documentation {
        Gets the header value associated with the given header name.

        P{{headerName}} Represents header name
        R{{}} Header value associated with the given header name as a `string`. If multiple header values are
              present, then the first value is returned. An exception is thrown if no header is found. Use
              `hasHeader()` beforehand to check the existence of header.
    }
    public extern function getHeader(@sensitive string headerName) returns @tainted string;

    documentation {
        Gets all the header values associated with the given header name.

        P{{headerName}} The header name
        R{{}} All the header values associated with the given header name as a `string[]`. An exception is thrown
              if no header is found. Use `hasHeader()` beforehand to check the existence of header.
    }
    public extern function getHeaders(@sensitive string headerName) returns @tainted string[];

    documentation {
        Gets all header names.

        R{{}} All header names as a `string[]`
    }
    public extern function getHeaderNames() returns @tainted string[];

    documentation {
        Adds the given header value against the given header.

        P{{headerName}} The header name
        P{{headerValue}} Represents the header value to be added
    }
    public extern function addHeader(@sensitive string headerName, string headerValue);

    documentation {
        Sets the given header value against the existing header. If a header already exists, its value is replaced
        with the given header value.

        P{{headerName}} The header name
        P{{headerValue}} Represents the header value
    }
    public extern function setHeader(@sensitive string headerName, string headerValue);

    documentation {
        Removes the given header from the entity.

        P{{headerName}} Represents the header name
    }
    public extern function removeHeader(@sensitive string headerName);

    documentation {
        Removes all headers associated with the entity.
    }
    public extern function removeAllHeaders();

    documentation {
        Checks whether the requested header key exists in the header map.

        P{{headerName}} The header name
        R{{}} True if the specified header key exists
    }
    public extern function hasHeader(@sensitive string headerName) returns boolean;
};

function Entity::setFileAsEntityBody(@sensitive string filePath,
                                            @sensitive string contentType = "application/octet-stream") {
    io:ByteChannel channel = io:openFile(filePath, READ_PERMISSION);
    self.setByteChannel(channel, contentType = contentType);
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

documentation {
    Encodes a given input with MIME specific Base64 encoding scheme.

    P{{contentToBeEncoded}} Content that needs to be encoded can be of type `string`, `byte[]` or `io ByteChannel`
    P{{charset}} Charset to be used. This is used only with the string input

    R{{}} If the given input is of type string, an encoded `string` is returned
          If the given input is of type byte[], an encoded `byte[]` is returned
          If the given input is of type io:ByteChannel, an encoded `io:ByteChannel` is returned
          In case of errors, an `error` record is returned
}
extern function base64Encode((string|byte[]|io:ByteChannel) contentToBeEncoded, string charset = "utf-8")
    returns (string|byte[]|io:ByteChannel|error);

documentation {
    Decodes a given input with MIME specific Base64 encoding scheme.

    P{{contentToBeDecoded}} Content that needs to be decoded can be of type `string`, `byte[]` or `io ByteChannel`
    P{{charset}} Charset to be used. This is used only with the string input
    R{{}} If the given input is of type string, a decoded `string` is returned
          If the given input is of type byte[], a decoded `byte[]` is returned
          If the given input is of type io:ByteChannel, a decoded `io:ByteChannel` is returned
          In case of errors, an `error` record is returned
}
extern function base64Decode((string|byte[]|io:ByteChannel) contentToBeDecoded, string charset = "utf-8")
    returns (string|byte[]|io:ByteChannel|error);

documentation {
    Encodes a given byte[] with Base64 encoding scheme.

    P{{valueToBeEncoded}} Content that needs to be encoded
    R{{}} An encoded byte[]. In case of errors, an `error` record is returned
}
public function base64EncodeBlob(byte[] valueToBeEncoded) returns byte[]|error {
    error customErr = {message:"Error occurred while encoding byte[]"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        byte[] returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        error encodeErr => return encodeErr;
    }
}

documentation {
    Encodes a given string with Base64 encoding scheme.

    P{{valueToBeEncoded}} Content that needs to be encoded
    P{{charset}} Charset to be used
    R{{}} An encoded `string`. In case of errors, an `error` record is returned
}
public function base64EncodeString(string valueToBeEncoded, string charset = "utf-8") returns string|error {
    error customErr = {message:"Error occurred while encoding string"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return returnString;
        byte[] returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        error encodeErr => return encodeErr;
    }
}

documentation {
    Encodes a given ByteChannel with Base64 encoding scheme.

    P{{valueToBeEncoded}} Content that needs to be encoded
    R{{}} An encoded `io:ByteChannel`. In case of errors, an `error` record is returned
}
public function base64EncodeByteChannel(io:ByteChannel valueToBeEncoded) returns io:ByteChannel|error {
    error customErr = {message:"Error occurred while encoding ByteChannel content"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        byte[] returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        error encodeErr => return encodeErr;
    }
}

documentation {
    Decodes a given byte[] with Base64 encoding scheme.

    P{{valueToBeDecoded}} Content that needs to be decoded
    R{{}} A decoded `byte[]`. In case of errors, an `error` record is returned
}
public function base64DecodeBlob(byte[] valueToBeDecoded) returns byte[]|error {
    error customErr = {message:"Error occurred while decoding byte[]"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        byte[] returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        error decodeErr => return decodeErr;
    }
}

documentation {
    Decodes a given string with Base64 encoding scheme.

    P{{valueToBeDecoded}} Content that needs to be decoded
    P{{charset}} Charset to be used
    R{{}} A decoded `string`. In case of errors, an `error` record is returned
}
public function base64DecodeString(string valueToBeDecoded, string charset = "utf-8") returns string|error {
    error customErr = {message:"Error occurred while decoding string"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return returnString;
        byte[] returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        error decodeErr => return decodeErr;
    }
}

documentation {
    Decodes a given ByteChannel with Base64 encoding scheme.

    P{{valueToBeDecoded}} Content that needs to be decoded
    R{{}} A decoded `io:ByteChannel`. In case of errors, an `error` record is returned
}
public function base64DecodeByteChannel(io:ByteChannel valueToBeDecoded) returns io:ByteChannel|error {
    error customErr = {message:"Error occurred while decoding ByteChannel content"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        byte[] returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        error decodeErr => return decodeErr;
    }
}

documentation {
    Gets the encoding value from a given MediaType.

    P{{contentType}} A MediaType struct
    R{{}} The encoding value as a `string`
}
function getEncoding(MediaType contentType) returns (string) {
    return contentType.parameters.CHARSET;
}

documentation {
    Given the Content-Type in string, gets the MediaType object populated with it.

    P{{contentType}} Content-Type in string
    R{{}} `MediaType` object or an error in case of error
}
public extern function getMediaType(string contentType) returns MediaType|error;

documentation {
    Given the Content-Disposition as a string, gets the ContentDisposition object with it.

    P{{contentDisposition}} Content disposition string
    R{{}} A `ContentDisposition` object
}
public extern function getContentDispositionObject(string contentDisposition) returns ContentDisposition;

documentation {
        Converts given byte[] to a string.

        P{{encoding}} Encoding to used in byte[] conversion to string
        R{{}} String representation of the given byte[]
    }
public extern function byteArrayToString(byte[] b, string encoding) returns string;

documentation {
        Encode a given byte[] with Base64 encoding scheme.

        R{{}} Return an encoded byte[]
    }
public extern function base64EncodeByteArray(byte[] b) returns byte[];

documentation {
        Decode a given byte[] with Base64 encoding scheme.

        R{{}} Return a decoded byte[]
    }
public extern function base64DecodeByteArray(byte[] b) returns byte[];

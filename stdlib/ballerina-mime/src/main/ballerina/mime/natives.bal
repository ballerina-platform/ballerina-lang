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
    Key name for 'boundary' parameter in MediaType. This is needed for composite type media types.
}
@final public string BOUNDARY = "boundary";

documentation {
    Key name for 'start' parameter in MediaType. This determines which part in the multipart message contains the 
    payload.
}
@final public string START = "start";

documentation {
    Key name for 'type' parameter in MediaType. This indicates the MIME media type of the 'root' body part.
}
@final public string TYPE = "type";

documentation {
    Key name for 'charset' parameter in MediaType. Indicate the character set of the body text.
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
    Represent 'content-id' header name.
}
@final public string CONTENT_ID = "content-id";

documentation {
    Represent 'content-length' header name.
}
@final public string CONTENT_LENGTH = "content-length";

documentation {
    Represent 'content-type' header name.
}
@final public string CONTENT_TYPE = "content-type";

documentation {
    Represent 'content-disposition' header name.
}
@final public string CONTENT_DISPOSITION = "content-disposition";

documentation {
    Represent values in Content-Disposition header.

    F{{fileName}} Default filename for storing the bodypart, if the receiving agent wishes to store it in an external
                  file
    F{{disposition}} Indicates how the body part should be presented (inline, attachment or as form-data)
    F{{name}} Represent the field name in case of 'multipart/form-data'
    F{{parameters}} A set of parameters, specified in attribute=value notation
}
public type ContentDisposition object {

    public {
        string fileName;
        string disposition;
        string name;
        map<string> parameters;
    }

    documentation {
        Convert the ContentDisposition type to a string suitable for use as the value of a corresponding MIME header.
        R{{}} Return the ContentDisposition object's content as a string
    }
    public native function toString() returns (string);
};

documentation {
    Describes the nature of the data in the body of a MIME entity.

    F{{primaryType}} Declares the general type of data
    F{{subType}} A specific format of the primary type data
    F{{suffix}} Identify the semantics of a specific media type
    F{{parameters}} A set of parameters, specified in an attribute=value notation
}
public type MediaType object {

    public {
        string primaryType;
        string subType;
        string suffix;
        map<string> parameters;
    }

    documentation {
        Get “primaryType/subtype+suffix” combination in string format.

        R{{}} Return base type from MediaType struct
    }
    public function getBaseType() returns (string);

    documentation {
        Convert the media type to a string suitable for use as the value of a corresponding HTTP header.
        R{{}} Return the Content-Type with parameters as a string
    }
    public function toString() returns (string);
};

public function MediaType::getBaseType() returns (string) {
    return self.primaryType + "/" + self.subType;
}

public function MediaType::toString() returns (string) {
    string contentType = self.getBaseType();
    map<string> parameters = self.parameters;
    string[] arrKeys = self.parameters.keys();
    int size = lengthof arrKeys;
    if (size > 0) {
        contentType = contentType + "; ";
    }
    int index = 0;
    while (index < size) {
        string value = parameters[arrKeys[index]];
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
    Represent all entity related errors.

    F{{message}} The error message
    F{{cause}} The error which caused the entity error
}
public type EntityError {
    string message,
    error? cause,
};

documentation {
    Represent the headers and body of a message. This can be used to represent both the entity of a top level message
    and an entity(body part) inside of a multipart entity.

    F{{contentType}} Describes the data contained in the body of the entity
    F{{contentId}} Helps one body of an entity to make a reference to another
    F{{contentLength}} Represent the size of the entity
    F{{contentDisposition}} Represent values related to Content-Disposition header
}
public type Entity object {

    private {
        MediaType contentType;
        string contentId;
        int contentLength;
        ContentDisposition contentDisposition;
    }

    documentation {
        Set the content-type to entity.

        P{{mediaType}} content-type that needs to be set to entity
    }
    public function setContentType(string mediaType) {
        self.contentType = check getMediaType(mediaType);
        self.setHeader(CONTENT_TYPE, mediaType);
    }

    documentation {
        Get the content-type of entity.

        R{{}} Return content-type as a string
    }
    public function getContentType() returns string {
        string contentTypeHeaderValue;
        if (self.hasHeader(CONTENT_TYPE)) {
            contentTypeHeaderValue = self.getHeader(CONTENT_TYPE);
        }
        return contentTypeHeaderValue;
    }

    documentation {
        Set the content-id of the entity.

        P{{contentId}} content-id that needs to be set to entity
    }
    public function setContentId(string contentId) {
        self.contentId = contentId;
        self.setHeader(CONTENT_ID, contentId);
    }

    documentation {
        Get the content-id of entity.

        R{{}} Return content-id as a string
    }
    public function getContentId() returns string {
        string contentId;
        if (self.hasHeader(CONTENT_ID)) {
            contentId = self.getHeader(CONTENT_ID);
        }
        return contentId;
    }

    documentation {
        Set the content-length of the entity.

        P{{contentLength}} content-length that needs to be set to entity
    }
    public function setContentLength(int contentLength) {
        self.contentLength = contentLength;
        var contentLengthStr = <string>contentLength;
        self.setHeader(CONTENT_LENGTH, contentLengthStr);
    }

    documentation {
        Get the content-length of entity.

        R{{}} Return content-length as an int
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
        Set the content-disposition of the entity.

        P{{contentDisposition}} content-disposition that needs to be set to entity
    }
    public function setContentDisposition(ContentDisposition contentDisposition) {
        self.contentDisposition = contentDisposition;
        self.setHeader(CONTENT_DISPOSITION, contentDisposition.toString());
    }

    documentation {
        Get the content-disposition of entity.

        R{{}} Return ContentDisposition object
    }
    public function getContentDisposition() returns ContentDisposition {
        string contentDispositionVal;
        if (self.hasHeader(CONTENT_DISPOSITION)) {
            contentDispositionVal = self.getHeader(CONTENT_DISPOSITION);
        }
        return getContentDispositionObject(contentDispositionVal);
    }

    documentation {
        Set the entity body with a given content.
    }
    public function setBody((string|xml|json|blob|io:ByteChannel|Entity[]) entityBody);

    documentation {
        Set the entity body with a given file handler.

        P{{filePath}} Represent a file
    }
    public function setFileAsEntityBody(@sensitive string filePath, string contentType = "application/octec-stream");

    documentation {
        Set the entity body with the given json content.

        P{{jsonContent}} Json content that needs to be set to entity
    }
    public native function setJson(json jsonContent, string contentType = "application/json");

    documentation {
        Given an entity, get the entity body in json form.

        R{{}} Return json data
        R{{}} EntityError will get thrown in case of errors during data-source extraction from entity
    }
    public native function getJson() returns @tainted json|EntityError;

    documentation {
        Set the entity body with the given xml content
        P{{xmlContent}} Xml content that needs to be set to entity
    }
    public native function setXml(xml xmlContent, string contentType = "application/xml");

    documentation {
        Given an entity, get the entity body in xml form.

        R{{}} Return xml data
        R{{}} EntityError will get thrown in case of errors during data-source extraction from entity
    }
    public native function getXml() returns @tainted xml|EntityError;

    documentation {
        Set the entity body with the given text content.

        P{{textContent}} Text content that needs to be set to entity
    }
    public native function setText(string textContent, string contentType = "text/plain");

    documentation {
        Given an entity, get the entity body in text form.

        R{{}} Return text data
        R{{}} EntityError will get thrown in case of errors during data-source extraction from entity
    }
    public native function getText() returns @tainted string|EntityError;

    documentation {
        Set the entity body with the given blob content.

        P{{blobContent}} Blob content that needs to be set to entity
        R{{}} Return a blob
    }
    public native function setBlob(blob blobContent, string contentType = "application/octec-stream");

    documentation {
        Given an entity, get the entity body as a blob. If the entity size is considerably large consider
        using getEntityWrapper() method instead.

        R{{}} Return a blob
        R{{}} EntityError will get thrown in case of errors during data-source extraction from entity
    }
    public native function getBlob() returns @tainted blob|EntityError;

    documentation {
        Set the entity body with the given byte channel content.

        P{{byteChannel}} Byte channel that needs to be set to entity
    }
    public native function setByteChannel(io:ByteChannel byteChannel, string contentType = "application/octec-stream");

    documentation {
        Given an entity, get the entity body as a byte channel.

        R{{}} Return a byte channel
        R{{}} EntityError will get thrown in case of errors during byte channel extraction from entity
    }
    public native function getByteChannel() returns @tainted io:ByteChannel|EntityError;

    documentation {
        Given an entity, get its body parts.

        R{{}} Return an array of entities which represent its body parts
        R{{}} EntityError will get thrown in case of errors during data-source extraction from entity
    }
    public native function getBodyParts() returns Entity[]|EntityError;

    documentation {
        Given an entity, get the body parts as a byte channel.

        R{{}} Return body parts as a byte channel
    }
    public native function getBodyPartsAsChannel() returns @tainted io:ByteChannel;

    documentation {
        Set body parts to entity.

        P{{bodyParts}} Represent the body parts that needs to be set to the entity
    }
    public native function setBodyParts(Entity[] bodyParts, string contentType = "multipart/form-data");

    documentation {
        Get the header value associated with the given header name.

        P{{headerName}} Represent header name
        R{{}} Return header value associated with the given header name. If multiple header values are present, then
              the first value will be returned
    }
    public native function getHeader(@sensitive string headerName) returns @tainted string;

    documentation {
        Get all the header values associated with the given header name.

        P{{headerName}} Represent the header name
        R{{}} Return all the header values associated with the given header name as a string of arrays
    }
    public native function getHeaders(@sensitive string headerName) returns @tainted string[];

    documentation {
        Get all header names.

        R{{}} Return all header names as an array of strings
    }
    public native function getHeaderNames() returns @tainted string[];

    documentation {
        Add the given header value against the given header.

        P{{headerName}} Represent the header name
        P{{headerValue}} Represent the header value to be added
    }
    public native function addHeader(@sensitive string headerName, string headerValue);

    documentation {
        Set the given header value against the given header. If a header already exist, its value will be replaced
        with the given header value.

        P{{headerName}} Represent the header name
        P{{headerValue}} Represent the header value
    }
    public native function setHeader(@sensitive string headerName, string headerValue);

    documentation {
        Remove the given header from the entity.

        P{{headerName}} Represent the header name
    }
    public native function removeHeader(@sensitive string headerName);

    documentation {
        Remove all headers associated with the entity.
    }
    public native function removeAllHeaders();

    documentation {
        Check the header existence.
    }
    public native function hasHeader(@sensitive string headerName) returns boolean;
};

public function Entity::setFileAsEntityBody(@sensitive string filePath,
                                            string contentType = "application/octec-stream") {
    io:ByteChannel channel = io:openFile(filePath, READ_PERMISSION);
    self.setByteChannel(channel, contentType = contentType);
}

public function Entity::setBody((string|xml|json|blob|io:ByteChannel|Entity[]) entityBody) {
    match entityBody {
        string textContent => self.setText(textContent);
        xml xmlContent => self.setXml(xmlContent);
        json jsonContent => self.setJson(jsonContent);
        blob blobContent => self.setBlob(blobContent);
        io:ByteChannel byteChannelContent => self.setByteChannel(byteChannelContent);
        Entity[] bodyParts => self.setBodyParts(bodyParts);
    }
}

documentation {
    Represent errors related to mime base64 encoder.

    F{{message}} The error message
    F{{cause}} The cause of the error
}
public type Base64EncodeError {
    string message,
    error? cause,
};

documentation {
    Represent errors related to mime base64 decoder.

    F{{message}} The error message
    F{{cause}} The cause of the error
}
public type Base64DecodeError {
    string message,
    error? cause,
};

documentation {
    Encode a given input with MIME specific Base64 encoding scheme.

    P{{contentToBeEncoded}} Content that needs to be encoded can be of type string, blob or io ByteChannel
    P{{charset}} Charset to be used. This is used only with the string input
    R{{}} If the given input is of type string return value will be an encoded string
    R{{}} If the given input is of type blob return value will be an encoded blob
    R{{}} If the given input is of type io:ByteChannel return value will be an encoded io ByteChannel
    R{{}} Base64EncodeError will get return, in case of errors
}
native function base64Encode((string|blob|io:ByteChannel) contentToBeEncoded, string charset = "utf-8")
    returns (string|blob|io:ByteChannel|Base64EncodeError);

documentation {
    Decode a given input with MIME specific Base64 encoding scheme.

    P{{contentToBeDecoded}} Content that needs to be decoded can be of type string, blob or io ByteChannel
    P{{charset}} Charset to be used. This is used only with the string input
    R{{}} If the given input is of type string return value will be a decoded string
    R{{}} If the given input is of type blob return value will be a decoded blob
    R{{}} If the given input is of type io:ByteChannel return value will be a decoded io ByteChannel
    R{{}} Base64DecodeError will get return, in case of errors
}
native function base64Decode((string|blob|io:ByteChannel) contentToBeDecoded, string charset = "utf-8")
    returns (string|blob|io:ByteChannel|Base64DecodeError);

documentation {
    Encode a given blob with Base64 encoding scheme.

    P{{valueToBeEncoded}} Content that needs to be encoded
    R{{}} Return an encoded blob
    R{{}} Base64EncodeError will get return, in case of errors
}
public function base64EncodeBlob(blob valueToBeEncoded) returns blob|Base64EncodeError {
    Base64EncodeError customErr = {message:"Error occurred while encoding blob"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        blob returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        Base64EncodeError encodeErr => return encodeErr;
    }
}

documentation {
    Encode a given string with Base64 encoding scheme.

    P{{valueToBeEncoded}} Content that needs to be encoded
    P{{charset}} Charset to be used
    R{{}} Return an encoded string
    R{{}} Base64EncodeError will get return, in case of errors
}
public function base64EncodeString(string valueToBeEncoded, string charset = "utf-8") returns string|Base64EncodeError {
    Base64EncodeError customErr = {message:"Error occurred while encoding string"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return returnString;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        Base64EncodeError encodeErr => return encodeErr;
    }
}

documentation {
    Encode a given ByteChannel with Base64 encoding scheme.

    P{{valueToBeEncoded}} Content that needs to be encoded
    R{{}} Return an encoded ByteChannel
    R{{}} Base64EncodeError will get return, in case of errors
}
public function base64EncodeByteChannel(io:ByteChannel valueToBeEncoded) returns io:ByteChannel|Base64EncodeError {
    Base64EncodeError customErr = {message:"Error occurred while encoding ByteChannel content"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        Base64EncodeError encodeErr => return encodeErr;
    }
}

documentation {
    Decode a given blob with Base64 encoding scheme.

    P{{valueToBeDecoded}} Content that needs to be decoded
    R{{}} Return a decoded blob
    R{{}} Base64DecodeError will get return, in case of errors
}
public function base64DecodeBlob(blob valueToBeDecoded) returns blob|Base64DecodeError {
    Base64DecodeError customErr = {message:"Error occurred while decoding blob"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        blob returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

documentation {
    Decode a given string with Base64 encoding scheme.

    P{{valueToBeDecoded}} Content that needs to be decoded
    P{{charset}} Charset to be used
    R{{}} Return a decoded string
    R{{}} Base64DecodeError will get return, in case of errors
}
public function base64DecodeString(string valueToBeDecoded, string charset = "utf-8") returns string|Base64DecodeError {
    Base64DecodeError customErr = {message:"Error occurred while decoding string"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return returnString;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

documentation {
    Decode a given ByteChannel with Base64 encoding scheme.

    P{{valueToBeDecoded}} Content that needs to be decoded
    R{{}} Return a decoded ByteChannel
    R{{}} Base64DecodeError will get return, in case of errors
}
public function base64DecodeByteChannel(io:ByteChannel valueToBeDecoded) returns io:ByteChannel|Base64DecodeError {
    Base64DecodeError customErr = {message:"Error occurred while decoding ByteChannel content"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

documentation {
    Get the encoding value from a given MediaType.

    P{{contentType}} A MediaType struct
    R{{}} Return encoding value
}
function getEncoding(MediaType contentType) returns (string) {
    return contentType.parameters.CHARSET;
}

documentation {
    Given the Content-Type in string, get the MediaType object populated with it.

    P{{contentType}} Content-Type in string
    R{{}} Return MediaType struct or an error in case of error
}
public native function getMediaType(string contentType) returns MediaType|error;

documentation {
    Given the Content-Disposition as a string, get the ContentDisposition struct object with it.

    P{{contentDisposition}} content disposition string
    R{{}} Return ContentDisposition object
}
public native function getContentDispositionObject(string contentDisposition) returns ContentDisposition;

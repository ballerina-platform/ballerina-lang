package ballerina.mime;

import ballerina/file;
import ballerina/io;

@Description {value:"Key name for 'boundary' parameter in MediaType. This is needed for composite type media types"}
@final public string BOUNDARY = "boundary";

@Description {value:"Key name for 'start' parameter in MediaType. This determines which part in the multipart message contains the payload"}
@final public string START = "start";

@Description {value:"Key name for 'type' parameter in MediaType. This indicates the MIME media type of the 'root' body part"}
@final public string TYPE = "type";

@Description {value:"Key name for 'charset' parameter in MediaType. Indicate the character set of the body text"}
@final public string CHARSET = "charset";

@Description {value:"Default charset to be used with MIME encoding and decoding"}
@final public string DEFAULT_CHARSET = "UTF-8";

@Description {value:"Permission to be used with opening a byte channel for overflow data"}
@final io:Mode READ_PERMISSION = "r";

@Description {value:"Represent 'content-id' header name"}
@final public string CONTENT_ID = "content-id";

@Description {value:"Represent 'content-length' header name"}
@final public string CONTENT_LENGTH = "content-length";

@Description {value:"Represent 'content-type' header name"}
@final public string CONTENT_TYPE = "content-type";

@Description {value:"Represent 'content-disposition' header name"}
@final public string CONTENT_DISPOSITION = "content-disposition";

@Description {value:"Represent values in Content-Disposition header"}
@Field {value:"fileName: Default filename for storing the bodypart, if the receiving agent wishes to store it in an external file"}
@Field {value:"disposition: Indicates how the body part should be presented (inline, attachment or as form-data)"}
@Field {value:"name: Represent the field name in case of 'multipart/form-data'"}
@Field {value:"parameters: A set of parameters, specified in attribute=value notation"}
public type ContentDisposition object {
   public {
       string fileName;
       string disposition;
       string name;
       map<string> parameters;
   }

   @Description {value:"Convert the ContentDisposition type to a string suitable for use as the value of a corresponding MIME header."}
   @Return {value:"Return the ContentDisposition object's content as a string"}
   public native function toString () returns (string);
};

@Description {value:"Describes the nature of the data in the body of a MIME entity."}
@Field {value:"primaryType: Declares the general type of data"}
@Field {value:"subType: A specific format of the primary type data"}
@Field {value:"suffix: Identify the semantics of a specific media type"}
@Field {value:"parameters: A set of parameters, specified in an attribute=value notation"}
public type MediaType object {
    public {
        string primaryType;
        string subType;
        string suffix;
        map<string> parameters;
    }

    @Description {value:"Get “primaryType/subtype+suffix” combination in string format."}
    @Return {value:"Return base type from MediaType struct"}
    public function getBaseType () returns (string);

    @Description {value:"Convert the media type to a string suitable for use as the value of a corresponding HTTP header."}
    @Return {value:"Return the Content-Type with parameters as a string"}
    public function toString () returns (string);
};

public function MediaType::getBaseType () returns (string) {
    return self.primaryType + "/" + self.subType;
}

public function MediaType::toString () returns (string) {
    string contentType = self.getBaseType();
    map<string> parameters = self.parameters;
    string[] arrKeys = self.parameters.keys();
    int size = lengthof arrKeys;
    if(size > 0) {
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

@Description {value:"Represent all entity related errors"}
@Field {value:"message: The error message"}
@Field {value:"cause: The error which caused the entity error"}
public type EntityError  {
    string message,
    error? cause,
};

@Description {value:"Represent the headers and body of a message. This can be used to represent both the entity of a top
level message and an entity(body part) inside of a multipart entity."}
@Field {value:"contentType: Describes the data contained in the body of the entity"}
@Field {value:"contentId: Helps one body of an entity to make a reference to another"}
@Field {value:"size: Represent the size of the entity"}
@Field {value:"contentDisposition: Represent values related to Content-Disposition header"}
public type Entity object {
    private {
        MediaType contentType;
        string contentId;
        int contentLength;
        ContentDisposition contentDisposition;
    }

    public function setContentType (string mediaType) {
        self.contentType = check getMediaType(mediaType);
        self.setHeader(CONTENT_TYPE, mediaType);
    }

    public function getContentType () returns string {
        string contentTypeHeaderValue;
        if (self.hasHeader(CONTENT_TYPE)) {
            contentTypeHeaderValue = self.getHeader(CONTENT_TYPE);
        }
        return contentTypeHeaderValue;
    }

    public function setContentId(string contentId) {
        self.contentId = contentId;
        self.setHeader(CONTENT_ID, contentId);
    }

    public function getContentId() returns string {
        string contentId;
        if (self.hasHeader(CONTENT_ID)) {
            contentId = self.getHeader(CONTENT_ID);
        }
        return contentId;
    }

    public function setContentLength(int contentLength) {
        self.contentLength = contentLength;
        var contentLengthStr = <string>contentLength;
        self.setHeader(CONTENT_LENGTH, contentLengthStr);
    }

    public function getContentLength() returns int | error {
        string contentLength;
        if (self.hasHeader(CONTENT_LENGTH)) {
            contentLength = self.getHeader(CONTENT_LENGTH);
        }
        if(contentLength == "") {
            return -1;
        } else {
            return <int>contentLength;
        }
    }

    public function setContentDisposition (ContentDisposition contentDisposition) {
        self.contentDisposition = contentDisposition;
        self.setHeader(CONTENT_DISPOSITION, contentDisposition.toString());
    }

    public function getContentDisposition () returns ContentDisposition {
        string contentDispositionVal;
        if (self.hasHeader(CONTENT_DISPOSITION)) {
            contentDispositionVal = self.getHeader(CONTENT_DISPOSITION);
        }
        return getContentDispositionObject(contentDispositionVal);
    }

    @Description {value:"Set the entity body with a given content"}
    public function setBody ((string | xml | json | blob | io:ByteChannel | Entity[]) entityBody);

    @Description {value:"Set the entity body with a given file handler"}
    @Param {value:"fileHandler: Represent a file"}
    public function setFileAsEntityBody (@sensitive string filePath, string contentType = "application/octec-stream");

    @Description {value:"Set the entity body with the given json content"}
    @Param {value:"jsonContent: Json content that needs to be set to entity"}
    public native function setJson (json jsonContent, string contentType="application/json");

    @Description {value:"Given an entity, get the entity body in json form."}
    @Return {value:"Return json data"}
    @Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
    public native function getJson () returns @tainted json | EntityError;

    @Description {value:"Set the entity body with the given xml content"}
    @Param {value:"xmlContent: Xml content that needs to be set to entity"}
    public native function setXml (xml xmlContent, string contentType="application/xml");

    @Description {value:"Given an entity, get the entity body in xml form."}
    @Return {value:"Return xml data"}
    @Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
    public native function getXml () returns @tainted xml | EntityError;

    @Description {value:"Set the entity body with the given text content"}
    @Param {value:"textContent: Text content that needs to be set to entity"}
    public native function setText (string textContent, string contentType="text/plain");

    @Description {value:"Given an entity, get the entity body in text form."}
    @Return {value:"Return text data"}
    @Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
    public native function getText () returns @tainted string | EntityError;

    @Description {value:"Set the entity body with the given blob content"}
    @Param {value:"blobContent: Blob content that needs to be set to entity"}
    @Return {value:"Return a blob"}
    public native function setBlob (blob blobContent, string contentType="application/octec-stream");

    @Description {value:"Given an entity, get the entity body as a blob. If the entity size is considerably large consider
    using getEntityWrapper() method instead"}
    @Return {value:"Return a blob"}
    @Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
    public native function getBlob () returns @tainted blob | EntityError;

    @Description {value:"Set the entity body with the given byte channel content"}
    @Param {value:"byteChannel: Byte channel that needs to be set to entity"}
    public native function setByteChannel (io:ByteChannel byteChannel, string contentType="application/octec-stream");

    @Description {value:"Given an entity, get the entity body as a byte channel."}
    @Return {value:"Return a byte channel"}
    @Return {value:"EntityError will get thrown in case of errors during byte channel extraction from entity"}
    public native function getByteChannel () returns @tainted io:ByteChannel | EntityError;

    @Description {value:"Given an entity, get its body parts."}
    @Return {value:"Return an array of entities which represent its body parts"}
    @Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
    public native function getBodyParts () returns Entity[] | EntityError;

    @Description {value:"Given an entity, get the body parts as a byte channel."}
    @Param {value:"entity: Represent a MIME entity"}
    @Return {value:"Return body parts as a byte channel "}
    @Return {value:"EntityError will get thrown in case of errors"}
    public native function getBodyPartsAsChannel () returns @tainted io:ByteChannel;

    @Description {value:"Set body parts to entity"}
    @Param {value:"entity: Represent a MIME entity"}
    @Param {value:"bodyParts: Represent the body parts that needs to be set to the entity"}
    public native function setBodyParts (Entity[] bodyParts, string contentType="multipart/form-data");

    @Description {value:"Get the header value associated with the given header name"}
    @Param {value:"headerName: Represent header name"}
    @Return {value:"Return header value associated with the given header name. If multiple header values are present,
    then the first value will be returned"}
    public native function getHeader (@sensitive string headerName) returns @tainted string;

    @Description {value:"Get all the header values associated with the given header name"}
    @Param {value:"headerName: Represent the header name"}
    @Return {value:"Return all the header values associated with the given header name as a string of arrays"}
    public native function getHeaders (@sensitive string headerName) returns @tainted string[];

    @Description {value:"Get all header names."}
    @Return {value:"Return all header names as an array of strings"}
    public native function getHeaderNames () returns @tainted string[];

    @Description {value:"Add the given header value against the given header"}
    @Param {value:"headerName: Represent the header name"}
    @Param {value:"headerValue: Represent the header value to be added"}
    public native function addHeader (@sensitive string headerName, string headerValue);

    @Description {value:"Set the given header value against the given header. If a header already exist, its value will be
    replaced with the given header value"}
    @Param {value:"headerName: Represent the header name"}
    @Param {value:"headerValue: Represent the header value"}
    public native function setHeader (@sensitive string headerName, string headerValue);

    @Description {value:"Remove the given header from the entity"}
    @Param {value:"headerName: Represent the header name"}
    public native function removeHeader (@sensitive string headerName);

    @Description {value:"Remove all headers associated with the entity"}
    public native function removeAllHeaders ();

    @Description {value:"Check the header existence"}
    public native function hasHeader (@sensitive string headerName) returns boolean;
};

public function Entity::setFileAsEntityBody (@sensitive string filePath, string contentType = "application/octec-stream") {
    io:ByteChannel channel = io:openFile(filePath, READ_PERMISSION);
    self.setByteChannel(channel, contentType = contentType);
}

public function Entity::setBody ((string | xml | json | blob | io:ByteChannel | Entity[]) entityBody) {
    match entityBody {
        string textContent => self.setText(textContent);
        xml xmlContent => self.setXml(xmlContent);
        json jsonContent => self.setJson(jsonContent);
        blob blobContent => self.setBlob(blobContent);
        io:ByteChannel byteChannelContent => self.setByteChannel(byteChannelContent);
        Entity[] bodyParts => self.setBodyParts(bodyParts);
    }
}

@Description {value:"Represent errors related to mime base64 encoder"}
@Field {value:"message: The error message"}
@Field {value:"cause: The cause of the error"}
public type Base64EncodeError {
    string message,
    error? cause,
};

@Description {value:"Represent errors related to mime base64 decoder"}
@Field {value:"message: The error message"}
@Field {value:"cause: The cause of the error"}
public type Base64DecodeError {
    string message,
    error? cause,
};

@Description {value:"Encode a given input with MIME specific Base64 encoding scheme."}
@Param {value:"contentToBeEncoded: Content that needs to be encoded can be of type string, blob or io:ByteChannel"}
@Param {value:"charset: Charset to be used. This is used only with the string input"}
@Return {value:"If the given input is of type string return value will be an encoded string"}
@Return {value:"If the given input is of type blob return value will be an encoded blob"}
@Return {value:"If the given input is of type io:ByteChannel return value will be an encoded io:ByteChannel"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
native function base64Encode ((string | blob | io:ByteChannel) contentToBeEncoded, string charset="utf-8") returns (string  | blob  | io:ByteChannel | Base64EncodeError);

@Description {value:"Decode a given input with MIME specific Base64 encoding scheme."}
@Param {value:"contentToBeDecoded: Content that needs to be decoded can be of type string, blob or io:ByteChannel"}
@Param {value:"charset: Charset to be used. This is used only with the string input"}
@Return {value:"If the given input is of type string return value will be a decoded string"}
@Return {value:"If the given input is of type blob return value will be a decoded blob"}
@Return {value:"If the given input is of type io:ByteChannel return value will be a decoded io:ByteChannel"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
native function base64Decode ((string | blob | io:ByteChannel) contentToBeDecoded, string charset="utf-8") returns (string  | blob  | io:ByteChannel | Base64DecodeError);

@Description {value:"Encode a given blob with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Return {value:"Return an encoded blob"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeBlob(blob valueToBeEncoded) returns blob | Base64EncodeError {
    Base64EncodeError customErr = {message : "Error occurred while encoding blob"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        blob returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        Base64EncodeError encodeErr => return encodeErr;
    }
}
@Description {value:"Encode a given string with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Param {value:"charset: Charset to be used"}
@Return {value:"Return an encoded string"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeString(string valueToBeEncoded, string charset="utf-8") returns string | Base64EncodeError {
    Base64EncodeError customErr = {message : "Error occurred while encoding string"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return returnString;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        Base64EncodeError encodeErr => return encodeErr;
    }
}

@Description {value:"Encode a given ByteChannel with Base64 encoding scheme."}
@Param {value:"valueToBeEncoded: Content that needs to be encoded"}
@Return {value:"Return an encoded ByteChannel"}
@Return {value:"Base64EncodeError will get return, in case of errors"}
public function base64EncodeByteChannel(io:ByteChannel valueToBeEncoded) returns io:ByteChannel | Base64EncodeError {
    Base64EncodeError customErr = {message : "Error occurred while encoding ByteChannel content"};
    match base64Encode(valueToBeEncoded) {
        string returnString => return customErr;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        Base64EncodeError encodeErr => return encodeErr;
    }
}

@Description {value:"Decode a given blob with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Return {value:"Return a decoded blob"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeBlob(blob valueToBeDecoded) returns blob | Base64DecodeError {
    Base64DecodeError customErr = {message : "Error occurred while decoding blob"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        blob returnBlob => return returnBlob;
        io:ByteChannel returnChannel => return customErr;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

@Description {value:"Decode a given string with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Param {value:"charset: Charset to be used"}
@Return {value:"Return a decoded string"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeString(string valueToBeDecoded, string charset="utf-8") returns string | Base64DecodeError {
    Base64DecodeError customErr = {message : "Error occurred while decoding string"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return returnString;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return customErr;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

@Description {value:"Decode a given ByteChannel with Base64 encoding scheme."}
@Param {value:"valueToBeDecoded: Content that needs to be decoded"}
@Return {value:"Return a decoded ByteChannel"}
@Return {value:"Base64DecodeError will get return, in case of errors"}
public function base64DecodeByteChannel(io:ByteChannel valueToBeDecoded) returns io:ByteChannel | Base64DecodeError {
    Base64DecodeError customErr = {message : "Error occurred while decoding ByteChannel content"};
    match base64Decode(valueToBeDecoded) {
        string returnString => return customErr;
        blob returnBlob => return customErr;
        io:ByteChannel returnChannel => return returnChannel;
        Base64DecodeError decodeErr => return decodeErr;
    }
}

@Description {value:"Get the encoding value from a given MediaType."}
@Param {value:"contentType: A MediaType struct"}
@Return {value:"Return encoding value"}
function getEncoding (MediaType contentType) returns (string) {
    return contentType.parameters.CHARSET;
}

@Description {value:"Given the Content-Type in string, get the MediaType object populated with it."}
@Param {value:"contentType: Content-Type in string"}
@Return {value:"Return MediaType struct or an error in case of error"}
public native function getMediaType (string contentType) returns MediaType | error;

@Description {value:"Given the Content-Disposition as a string, get the ContentDisposition struct object with it."}
@Param {value:"contentType: Content-Type in string"}
@Return {value:"Return MediaType struct"}
public native function getContentDispositionObject (string contentDisposition) returns ContentDisposition;

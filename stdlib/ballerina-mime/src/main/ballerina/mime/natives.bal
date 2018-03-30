package ballerina.mime;

import ballerina/file;
import ballerina/io;

@Description {value:"Describes the nature of the data in the body of a MIME entity."}
@Field {value:"primaryType: Declares the general type of data"}
@Field {value:"subType: A specific format of the primary type data"}
@Field {value:"suffix: Identify the semantics of a specific media type"}
@Field {value:"parameters: A set of parameters, specified in an attribute=value notation"}
public struct MediaType {
    string primaryType;
    string subType;
    string suffix;
    map<string> parameters;
}

@Description {value:"Represent values in Content-Disposition header"}
@Field {value:"fileName: Default filename for storing the bodypart, if the receiving agent wishes to store it in an
external file"}
@Field {value:"disposition: Indicates how the body part should be presented (inline, attachment or as
form-data)"}
@Field {value:"name: Represent the field name in case of 'multipart/form-data'"}
@Field {value:"parameters: A set of parameters, specified in attribute=value notation"}
public struct ContentDisposition {
    string fileName;
    string disposition;
    string name;
    map parameters;
}

@Description {value:"Represent the headers and body of a message. This can be used to represent both the entity of a top
level message and an entity(body part) inside of a multipart entity."}
@Field {value:"contentType: Describes the data contained in the body of the entity"}
@Field {value:"contentId: Helps one body of an entity to make a reference to another"}
@Field {value:"size: Represent the size of the entity"}
@Field {value:"contentDisposition: Represent values related to Content-Disposition header"}
public struct Entity {
    MediaType contentType;
    string contentId;
    int size;
    ContentDisposition contentDisposition;
}

@Description {value:"Represent all entity related errors"}
@Field {value:"message: The error message"}
@Field {value:"cause: The error which caused the entity error"}
public struct EntityError {
    string message;
    error[] cause;
}

@Description {value:"Set the entity body with a given file handler"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"fileHandler: Represent a file"}
public function <Entity entity> setFileAsEntityBody (file:File fileHandler) {
    io:ByteChannel byteChannel = fileHandler.openChannel(READ_PERMISSION);
    entity.setByteChannel(byteChannel);
}

@Description {value:"Set the entity body with the given json content"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"jsonContent: Json content that needs to be set to entity"}
public native function <Entity entity> setJson (json jsonContent);

@Description {value:"Given an entity, get the entity body in json form."}
@Param {value:"entity: Represent a MIME entity"}
@Return {value:"Return json data"}
@Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
public native function <Entity entity> getJson () returns json | EntityError;

@Description {value:"Set the entity body with the given xml content"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"xmlContent: Xml content that needs to be set to entity"}
public native function <Entity entity> setXml (xml xmlContent);

@Description {value:"Given an entity, get the entity body in xml form."}
@Param {value:"entity: Represent a MIME entity"}
@Return {value:"Return xml data"}
@Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
public native function <Entity entity> getXml () returns xml | EntityError;

@Description {value:"Set the entity body with the given text content"}
@Param {value:"textContent: Text content that needs to be set to entity"}
public native function <Entity entity> setText (string textContent);

@Description {value:"Given an entity, get the entity body in text form."}
@Param {value:"entity: Represent a MIME entity"}
@Return {value:"Return text data"}
@Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
public native function <Entity entity> getText () returns string | EntityError;

@Description {value:"Set the entity body with the given blob content"}
@Param {value:"blobContent: Blob content that needs to be set to entity"}
@Return {value:"Return a blob"}
public native function <Entity entity> setBlob (blob blobContent);

@Description {value:"Given an entity, get the entity body as a blob. If the entity size is considerably large consider
using getEntityWrapper() method instead"}
@Param {value:"entity: Represent a MIME entity"}
@Return {value:"Return a blob"}
@Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
public native function <Entity entity> getBlob () returns blob | EntityError;

@Description {value:"Set the entity body with the given byte channel content"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"byteChannel: Byte channel that needs to be set to entity"}
public native function <Entity entity> setByteChannel (io:ByteChannel byteChannel);

@Description {value:"Given an entity, get the entity body as a byte channel."}
@Param {value:"entity: Represent a MIME entity"}
@Return {value:"Return a byte channel"}
@Return {value:"EntityError will get thrown in case of errors during byte channel extraction from entity"}
public native function <Entity entity> getByteChannel () returns io:ByteChannel | EntityError;

@Description {value:"Given an entity, get its body parts."}
@Param {value:"entity: Represent a MIME entity"}
@Return {value:"Return an array of entities which represent its body parts"}
@Return {value:"EntityError will get thrown in case of errors during data-source extraction from entity"}
public native function <Entity entity> getBodyParts () returns Entity[] | EntityError;

@Description {value:"Set body parts to entity"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"bodyParts: Represent the body parts that needs to be set to the entity"}
public native function <Entity entity> setBodyParts (Entity[] bodyParts);

@Description {value:"Given the Content-Type in string, get the MediaType struct populated with it."}
@Param {value:"contentType: Content-Type in string"}
@Return {value:"Return MediaType struct"}
public native function getMediaType (string contentType) returns (MediaType);

@Description {value:"Get “primaryType/subtype+suffix” combination in string format."}
@Param {value:"mediaType: MediaType struct"}
@Return {value:"Return base type from MediaType struct"}
public function <MediaType mediaType> toString () returns (string) {
    return mediaType.primaryType + "/" + mediaType.subType;
}

@Description {value:"Convert the media type to a string suitable for use as the value of a corresponding HTTP header."}
@Param {value:"mediaType: MediaType struct"}
@Return {value:"Return the Content-Type with parameters as a string"}
public function <MediaType mediaType> toStringWithParameters () returns (string) {
    string contentType = mediaType.toString() + "; ";
    map<string> parameters = mediaType.parameters;
    string[] arrKeys = mediaType.parameters.keys();
    int size = lengthof arrKeys;
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

@Description {value:"Represent MIME specific base64 encoder. This follows RFC 2045 for encoding operation."}
public struct MimeBase64Encoder {
}

@Description {value:"Represent MIME specific base64 decoder. This follows RFC 2045 for decoding operation."}
public struct MimeBase64Decoder {
}

@Description {value:"Represent quoated-printable encoder."}
public struct QuotedPrintableEncoder {
}

@Description {value:"Represent quoated-printable decoder."}
public struct QuotedPrintableDecoder {
}

@Description {value:"Encode byte array using MIME Base64 encoding scheme."}
@Param {value:"encoder: Represent MIME specific base64 encoder"}
@Param {value:"content: the byte array to encode"}
@Return {value:"Return resulting encoded bytes"}
public native function <MimeBase64Encoder encoder> encode (blob content) returns (blob);

@Description {value:"Encode a given string using MIME Base64 encoding scheme. First the given string will be
converted to a byte array with the given charset encoding. If the charset given is null default 'UTF-8' will be used.
 Then that byte array will be encoded using MIME Base64 encoding scheme and a new string will be constructed with the
  given charset."}
@Param {value:"encoder: Represent MIME specific base64 encoder"}
@Param {value:"content: string to encode"}
@Param {value:"charset: charset used in the given string and the resulting string"}
@Return {value:"Return resulting encoded string"}
public native function <MimeBase64Encoder encoder> encodeString (string content, string charset) returns (string);

@Description {value:"Decode byte array using MIME Base64 encoding scheme."}
@Param {value:"encoder: Represent MIME specific base64 decoder"}
@Param {value:"content: the byte array to decode"}
@Return {value:"Return resulting decoded bytes"}
public native function <MimeBase64Decoder decoder> decode (blob content) returns (blob);

@Description {value:"Decode a given string using MIME Base64 decoding scheme. First the given string will be
converted to a byte array with the given charset encoding. If the charset given is null default 'UTF-8' will be used.
 Then that byte array will be decoded using MIME Base64 decoding scheme and a new string will be constructed with the
  given charset."}
@Param {value:"encoder: Represent MIME specific base64 decoder"}
@Param {value:"content: string to decode"}
@Param {value:"charset: charset used in the given string and the resulting string"}
@Return {value:"Return resulting decoded string"}
public native function <MimeBase64Decoder decoder> decodeString (string content, string charset) returns (string);

@Description {value:"Get the encoding value from a given MediaType."}
@Param {value:"contentType: A MediaType struct"}
@Return {value:"Return encoding value"}
function getEncoding (MediaType contentType) returns (string) {
    return contentType.parameters.CHARSET;
}

@Description {value:"Represent 'application/x-www-form-urlencoded' media type value"}
public const string APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

@Description {value:"Represent 'application/json' media type value"}
public const string APPLICATION_JSON = "application/json";

@Description {value:"Represent 'application/octet-stream' media type value"}
public const string APPLICATION_OCTET_STREAM = "application/octet-stream";

@Description {value:"Represent 'application/svg+xml' media type value"}
public const string APPLICATION_SVG_XML = "application/svg+xml";

@Description {value:"Represent 'application/xhtml+xml' media type value"}
public const string APPLICATION_XHTML_XML = "application/xhtml+xml";

@Description {value:"Represent 'application/xml' media type value"}
public const string APPLICATION_XML = "application/xml";

@Description {value:"Represent 'multipart/form-data' media type value"}
public const string MULTIPART_FORM_DATA = "multipart/form-data";

@Description {value:"Represent 'multipart/mixed' media type value"}
public const string MULTIPART_MIXED = "multipart/mixed";

@Description {value:"Represent 'text/html' media type value"}
public const string TEXT_HTML = "text/html";

@Description {value:"Represent 'text/plain' media type value"}
public const string TEXT_PLAIN = "text/plain";

@Description {value:"Represent 'text/xml' media type value"}
public const string TEXT_XML = "text/xml";

@Description {value:"Key name for 'boundary' parameter in MediaType. This is needed for composite type media types"}
public const string BOUNDARY = "boundary";

@Description {value:"Key name for 'start' parameter in MediaType. This determines which part in the multipart message
contains the payload"}
public const string START = "start";

@Description {value:"Key name for 'type' parameter in MediaType. This indicates the MIME media type of the 'root'
body part"}
public const string TYPE = "type";

@Description {value:"Key name for 'charset' parameter in MediaType. Indicate the character set of the body text"}
public const string CHARSET = "charset";

@Description {value:"Default charset to be used with MIME encoding and decoding"}
public const string DEFAULT_CHARSET = "UTF-8";

@Description {value:"Permission to be used with opening a byte channel for overflow data"}
const string READ_PERMISSION = "r";

@Description {value:"Represent 'content-type' header name"}
public const string CONTENT_TYPE = "content-type";

@Description {value:"Get the header value associated with the given header name"}
@Param {value:"entity: Represent the MIME entity"}
@Param {value:"headerName: Represent header name"}
@Return {value:"Return header value associated with the given header name. If multiple header values are present,
then the first value will be returned"}
public native function <Entity entity> getHeader (string headerName) returns string;

@Description {value:"Get all the header values associated with the given header name"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"headerName: Represent the header name"}
@Return {value:"Return all the header values associated with the given header name as a string of arrays"}
public native function <Entity entity> getHeaders (string headerName) returns string[];

@Description {value:"Get all the headers as a map. Please note that manipulating the returned map has no effect to the original copy of headers"}
@Param {value:"entity: Represent a MIME entity"}
@Return {value:"Return a copy of all headers as a map."}
public native function <Entity entity> getCopyOfAllHeaders () returns map;

@Description {value:"Add the given header value against the given header"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"headerName: Represent the header name"}
@Param {value:"headerValue: Represent the header value to be added"}
public native function <Entity entity> addHeader (string headerName, string headerValue);

@Description {value:"Set the given header value against the given header. If a header already exist, its value will be
replaced with the given header value"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"headerName: Represent the header name"}
@Param {value:"headerValue: Represent the header value"}
public native function <Entity entity> setHeader (string headerName, string headerValue);

@Description {value:"Remove the given header from the entity"}
@Param {value:"entity: Represent a MIME entity"}
@Param {value:"headerName: Represent the header name"}
public native function <Entity entity> removeHeader (string headerName);

@Description {value:"Remove all headers associated with the entity"}
@Param {value:"entity: Represent a MIME entity"}
public native function <Entity entity> removeAllHeaders ();

@Description {value:"Check the header existence"}
@Param {value:"entity: Represent a MIME entity"}
public native function <Entity entity> hasHeader (string headerName) returns boolean;
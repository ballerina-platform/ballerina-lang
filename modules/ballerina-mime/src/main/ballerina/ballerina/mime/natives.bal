package ballerina.mime;

import ballerina.file;
import ballerina.io;

@Description {value:"Describes the nature of the data in the body of a MIME entity."}
@Field {value:"primaryType: Declares the general type of data"}
@Field {value:"subType: A specific format of the primary type data"}
@Field {value:"suffix: Identify the semantics of a specific media type"}
@Field {value:"parameters: A set of parameters, specified in an attribute=value notation"}
public struct MediaType {
    string primaryType;
    string subType;
    string suffix;
    map parameters;
}

@Description {value:"Represents a header value."}
@Field {value:"value: The value of header"}
@Field {value:"param: The param map of header"}
public struct HeaderValue {
    string value;
    map param;
}

@Description {value:"Represent the headers and body of a message. This can be used to represent both the entity of a top
level message and an entity(body part) inside of a multipart entity."}
@Field {value:"contentType: Describes the data contained in the body of the entity"}
@Field {value:"contentId: Helps one body of an entity to make a reference to another"}
@Field {value:"headers: Denote general, request/response and entity related headers. Keys of the header map
should represent the header name and value will be the 'HeaderValue' struct"}
@Field {value:"textData: Contents of the body in string form if the content is of text type"}
@Field {value:"jsonData: Contents of the body in json form if the content is of json type"}
@Field {value:"xmlData: Contents of the body in xml form if the content is of xml type"}
@Field {value:"byteData: Contents of the body as a byte array"}
@Field {value:"overflowData: If the size of the entity exceeds the limit defined in ballerina service or the client
connector, contents will be saved in a temporary file and can be accessed through this file handler"}
@Field {value:"multipartData: Represent body parts(entities) of a multipart entity"}
@Field {value:"size: Represent the size of the entity"}
@Field {value:"fileName: Default filename for storing the bodypart, if the receiving agent wishes to store it in an
external file"}
@Field {value:"contentDisposition: Indicates how the body part should be presented (inline, attachment or as
form-data)"}
@Field {value:"name: Represent the field name in case of 'multipart/form-data'"}
public struct Entity {
    MediaType contentType;
    string contentId;
    map headers;
    string textData;
    json jsonData;
    xml xmlData;
    blob byteData;
    file:File overflowData;
    Entity[] multipartData;
    int size;
    string fileName;
    Disposition contentDisposition;
    string name;
}

@Description {value:"Indicates how the body part should be presented."}
@Field {value:"INLINE: Is intended to be displayed automatically upon display of the message"}
@Field {value:"ATTACHMENT: Indicate that the body part is separate from the main body of the top level message"}
@Field {value:"FORM_DATA: When the media type is multipart/form-data, content disposition will be 'form-data'"}
public enum Disposition {
    INLINE,
    ATTACHMENT,
    FORM_DATA
}

@Description {value:"Given an entity, get the text payload, either from the memory or from the file handler."}
@Param {value:"entity: Represent mime Entity"}
@Return {value:"return text data"}
public function getText (Entity entity) (string) {
    if (entity.textData != null && entity.textData != "") {
        return entity.textData;
    } else {
        file:File overFlowData = entity.overflowData;
        if (overFlowData != null) {
            string encoding = getEncoding(entity.contentType);
            io:ByteChannel channel = overFlowData.openChannel(file:AccessMode.R);
            io:CharacterChannel characterChannel = channel.toCharacterChannel(encoding);
            string characters = characterChannel.readAllCharacters();
            return characters;
        }
        return null;
    }
}

@Description {value:"Given an entity, get the json payload, either from the memory or from the file handler."}
@Param {value:"entity: Represent mime Entity"}
@Return {value:"return json data"}
public function getJson (Entity entity) (json) {
    if (entity.jsonData != null) {
        return entity.jsonData;
    } else {
        file:File overFlowData = entity.overflowData;
        if (overFlowData != null) {
            string encoding = getEncoding(entity.contentType);
            io:ByteChannel channel = overFlowData.openChannel(file:AccessMode.R);
            blob bytes = readAll(channel);
            string content = bytes.toString(encoding);
            var jsonContent, _ = <json>content;
            return jsonContent;
        }
        return null;
    }
}

@Description {value:"Given an entity, get the xml payload, either from the memory or from the file handler."}
@Param {value:"entity: Represent mime Entity"}
@Return {value:"return xml data"}
public function getXml (Entity entity) (xml) {
    if (entity.xmlData != null) {
        return entity.xmlData;
    } else {
        file:File overFlowData = entity.overflowData;
        if (overFlowData != null) {
            string encoding = getEncoding(entity.contentType);
            io:ByteChannel channel = overFlowData.openChannel(file:AccessMode.R);
            blob bytes = readAll(channel);
            string content = bytes.toString(encoding);
            var xmlContent, _ = <xml>content;
            return xmlContent;
        }
        return null;
    }
}

@Description {value:"Given an entity, get the content as a byte array, either from the memory or from the file
handler."}
@Param {value:"entity: Represent mime Entity"}
@Return {value:"return byte array"}
public function getBlob (Entity entity) (blob) {
    file:File overFlowData = entity.overflowData;
    if (overFlowData != null) {
        io:ByteChannel channel = overFlowData.openChannel(file:AccessMode.R);
        return readAll(channel);
    } else {
        return entity.byteData;
    }
}

@Description {value:"Given the Content-Type in string, get the MediaType struct populated with it."}
@Param {value:"contentType: Content-Type in string"}
@Return {value:"return MediaType struct"}
public native function getMediaType (string contentType) (MediaType);

@Description {value:"Get “primaryType/subtype+suffix” combination in string format."}
@Param {value:"mediaType: MediaType struct"}
@Return {value:"return base type from MediaType struct"}
public function <MediaType mediaType> toString () (string) {
    return mediaType.primaryType + "/" + mediaType.subType;
}

@Description {value:"Convert the media type to a string suitable for use as the value of a corresponding HTTP header."}
@Param {value:"mediaType: MediaType struct"}
@Return {value:"return the Content-Type with parameters as a string"}
public function <MediaType mediaType> toStringWithParameters () (string) {
    string contentType = mediaType.toString() + "; ";
    map parameters = mediaType.parameters;
    string[] arrKeys = mediaType.parameters.keys();
    int size = lengthof arrKeys;
    int index = 0;
    while (index < size) {
        var value, _ = (string)parameters[arrKeys[index]];
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
@Return {value:"return resulting encoded bytes"}
public native function <MimeBase64Encoder encoder> encode (blob content) (blob);

@Description {value:"Encode a given string using MIME Base64 encoding scheme. First the given string will be
converted to a byte array with the given charset encoding. If the charset given is null default 'UTF-8' will be used.
 Then that byte array will be encoded using MIME Base64 encoding scheme and a new string will be constructed with the
  given charset."}
@Param {value:"encoder: Represent MIME specific base64 encoder"}
@Param {value:"content: string to encode"}
@Param {value:"charset: charset used in the given string and the resulting string"}
@Return {value:"return resulting encoded string"}
public native function <MimeBase64Encoder encoder> encodeString (string content, string charset) (string);

@Description {value:"Decode byte array using MIME Base64 encoding scheme."}
@Param {value:"encoder: Represent MIME specific base64 decoder"}
@Param {value:"content: the byte array to decode"}
@Return {value:"return resulting decoded bytes"}
public native function <MimeBase64Decoder decoder> decode (blob content) (blob);

@Description {value:"Decode a given string using MIME Base64 decoding scheme. First the given string will be
converted to a byte array with the given charset encoding. If the charset given is null default 'UTF-8' will be used.
 Then that byte array will be decoded using MIME Base64 decoding scheme and a new string will be constructed with the
  given charset."}
@Param {value:"encoder: Represent MIME specific base64 decoder"}
@Param {value:"content: string to decode"}
@Param {value:"charset: charset used in the given string and the resulting string"}
@Return {value:"return resulting decoded string"}
public native function <MimeBase64Decoder decoder> decodeString (string content, string charset) (string);

@Description {value:"Utility function used internally to get the bytes from a given channel."}
@Param {value:"channel: A ByteChannel"}
@Return {value:"return a byte array"}
function readAll (io:ByteChannel channel) (blob) {
    blob bytes;
    bytes, _ = channel.readAllBytes();
    return bytes;
}

@Description {value:"Get the encoding value from a given MediaType."}
@Param {value:"contentType: A MediaType struct"}
@Return {value:"return encoding value"}
function getEncoding(MediaType contentType) (string){
    var encoding = DEFAULT_CHARSET;
    TypeCastError castErr;
    if (contentType != null) {
        encoding, castErr = (string)contentType.parameters.CHARSET;
        if (castErr != null) {
            encoding = DEFAULT_CHARSET;
        }
    }
    return encoding;
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

@Description {value:"Default charset to be used with mime encoding and decoding"}
public const string DEFAULT_CHARSET = "UTF-8";

@Description {value:"Permission to be used with opening a byte channel for overflow data"}
const string READ_PERMISSION = "r";

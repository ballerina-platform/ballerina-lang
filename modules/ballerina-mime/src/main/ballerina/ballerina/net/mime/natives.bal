package ballerina.net.mime;

import ballerina.file;
import ballerina.io;

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

public const string READ_PERMISSION = "r";

@Description {value:"Describes the nature of the data in the body of a MIME entity"}
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

@Description {value:"Header struct represent general, request/respose and entity headers"}
@Field {value:"value: Represent header value"}
@Field {value:"params: Represent parameters of the header"}
struct Header {
    string value;
    map params;
}

@Description {value:"Represent the headers and body of a message. This can be used to represent both the entity of a top
level message and an entity(body part) inside of a multipart entity"}
@Field {value:"contentType: Describes the data contained in the body of the entity"}
@Field {value:"contentId: Helps one body of an entity to make a reference to another"}
@Field {value:"headers: Denote general, request/response and entity related headers. Keys of the header map
should represent the header name and value will be the 'Header' struct"}
@Field {value:"isInMemory: A boolean to represent whether the body of the entity is in memory or in a temporary file"}
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
    boolean isInMemory;
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

@Description {value:"Indicates how the body part should be presented"}
@Field {value:"INLINE: Is intended to be displayed automatically upon display of the message"}
@Field {value:"ATTACHMENT: Indicate that the body part is separate from the main body of the top level message"}
@Field {value:"FORM_DATA: When the media type is multipart/form-data, content disposition will be 'form-data'"}
public enum Disposition {
    INLINE,
    ATTACHMENT,
    FORM_DATA
}

@Description {value:"Given an entity, get the text payload"}
@Param {value:"entity: "}
@Return {value:""}
public function getText (Entity entity) (string) {
    if (entity.isInMemory) {
        return entity.textData;
    } else {
        file:File overFlowData = entity.overflowData;
        var encoding, _ = (string)entity.contentType.parameters.CHARSET;
        io:ByteChannel channel = overFlowData.openChannel(READ_PERMISSION);
        io:CharacterChannel characterChannel = channel.toCharacterChannel(encoding);
        string characters = characterChannel.readAllCharacters();
        return characters;
    }
}

@Description {value:"Given an entity, get the json payload"}
@Param {value:"entity: "}
@Return {value:""}
public function getJson (Entity entity) (json) {
    if (entity.isInMemory) {
        return entity.jsonData;
    } else {
        file:File overFlowData = entity.overflowData;
        io:ByteChannel channel = overFlowData.openChannel(READ_PERMISSION);
        blob bytes = readAll(channel);
        var encoding, _ = (string)entity.contentType.parameters.CHARSET;
        string content = bytes.toString("UTF-8");
        TypeConversionError err;
        var jsonContent, err = <json>content;
        //if (err != null) {
        //    throw e;
        //}
        return jsonContent;
    }
}

@Description {value:"Given an entity, get the xml payload"}
@Param {value:"entity: "}
@Return {value:""}
public function getXml (Entity entity) (xml) {
    if (entity.isInMemory) {
        return entity.xmlData;
    } else {
        file:File overFlowData = entity.overflowData;
        io:ByteChannel channel = overFlowData.openChannel(READ_PERMISSION);
        blob bytes = readAll(channel);
        var encoding, _ = (string)entity.contentType.parameters.CHARSET;
        string content = bytes.toString("UTF-8");
        TypeConversionError err;
        var xmlContent, err = <xml>content;
        //  if (err != null) {
        //      throw e;
        //  }
        return xmlContent;
    }
}

@Description {value:"Given an entity, get the content as a byte array"}
@Param {value:"entity: "}
@Return {value:""}
public function getBlob (Entity entity) (blob) {
    if (entity.isInMemory) {
        return entity.byteData;
    } else {
        file:File overFlowData = entity.overflowData;
        io:ByteChannel channel = overFlowData.openChannel(READ_PERMISSION);
        return readAll(channel);
    }
}

public native function getMediaType (string contentType) (MediaType);

public function <MediaType mediaType> toString () (string) {
    return mediaType.primaryType + "/" + mediaType.subType;
}

public function <MediaType mediaType> toStringWithParameters () (string) {
    string contentType = "Content-Type: " + mediaType.toString() + "; ";
    map parameters = mediaType.parameters;
    string[] arrKeys = mediaType.parameters.keys();
    int size = lengthof arrKeys;
    int index = 0;
    while (index < size) {
        var value, _ =(string)parameters[arrKeys[index]];
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

struct MimeBase64Encoder {
}

struct MimeBase64Decoder {
}

struct QuotedPrintableEncoder {
}

struct QuotedPrintableDecoder {
}

public const string APPLICATION_JSON = "application/json";
public const string MULTIPART_FORM_DATA = "multipart/form-data";

function readAll (io:ByteChannel channel) (blob) {
    blob bytes;
    int numberOfBytesRead;
    bytes, numberOfBytesRead = channel.readAllBytes();
    return bytes;
}
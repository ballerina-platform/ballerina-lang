import ballerina.mime;

function testGetTextFromFile(mime:Entity entity) (string) {
    return mime:getText(entity);
}

function testGetJsonFromFile(mime:Entity entity) (json) {
    return mime:getJson(entity);
}

function testGetXmlFromFile(mime:Entity entity) (xml) {
    return mime:getXml(entity);
}

function testGetBlobFromFile(mime:Entity entity) (blob) {
    return mime:getBlob(entity);
}

function testGetMediaType (string contentType) (mime:MediaType) {
    return mime:getMediaType(contentType);
}

function testToStringOnMediaType (mime:MediaType mediaType) (string){
    return mediaType.toString();
}

function testToStringWithParametersOnMediaType(mime:MediaType mediaType) (string){
    return mediaType.toStringWithParameters();
}

function testMimeBase64Encode (blob value) (blob) {
    mime:MimeBase64Encoder encoder = {};
    return encoder.encode(value);
}

function testMimeBase64EncodeString (string content, string charset) (string) {
    mime:MimeBase64Encoder encoder = {};
    return encoder.encodeString(content, charset);
}

function testMimeBase64Decode (blob value) (blob)  {
    mime:MimeBase64Decoder decoder = {};
    return decoder.decode(value);
}

function testMimeBase64DecodeString (string content, string charset) (string) {
    mime:MimeBase64Decoder decoder = {};
    return decoder.decodeString(content, charset);
}

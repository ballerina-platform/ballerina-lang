import ballerina.mime;

//function testGetTextFromFile(mime:Entity entity) (string) {
//    return mime:getText(entity);
//}
//
//function testGetJsonFromFile(mime:Entity entity) (json) {
//    return mime:getJson(entity);
//}
//
//function testGetXmlFromFile(mime:Entity entity) (xml) {
//    return mime:getXml(entity);
//}
//
//function testGetBlobFromFile(mime:Entity entity) (blob) {
//    return mime:getBlob(entity);
//}

function testSetAndGetJson (json jsonContent) (json) {
    mime:Entity entity = {};
    entity.setJson(jsonContent);
    return entity.getJson();
}

function testGetJsonMultipleTimes (json jsonContent) (json) {
    mime:Entity entity = {};
    entity.setJson(jsonContent);
    json returnContent1 = entity.getJson();
    json returnContent2 = entity.getJson();
    json returnContent3 = entity.getJson();
    json returnContent = {concatContent:[returnContent1, returnContent2, returnContent3]};
    return returnContent;
}

function testGetMediaType (string contentType) (mime:MediaType) {
    return mime:getMediaType(contentType);
}

function testToStringOnMediaType (mime:MediaType mediaType) (string) {
    return mediaType.toString();
}

function testToStringWithParametersOnMediaType (mime:MediaType mediaType) (string) {
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

function testMimeBase64Decode (blob value) (blob) {
    mime:MimeBase64Decoder decoder = {};
    return decoder.decode(value);
}

function testMimeBase64DecodeString (string content, string charset) (string) {
    mime:MimeBase64Decoder decoder = {};
    return decoder.decodeString(content, charset);
}

//@http:configuration {basePath:"/test"}
//service<http> helloServer {
//    @http:resourceConfig {
//        methods:["POST"],
//        path:"/largepayload"
//    }
//    resource keepPayloadInTempFile (http:Connection conn, http:InRequest request) {
//        mime:Entity entity = request.getEntity();
//        http:OutResponse response = {};
//        response.setStringPayload(entity.overflowData.path);
//        _ = conn.respond(response);
//    }
//}

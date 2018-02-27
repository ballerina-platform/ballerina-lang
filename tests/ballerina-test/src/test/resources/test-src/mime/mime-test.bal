import ballerina.mime;
import ballerina.file;
import ballerina.io;
import ballerina.net.http;

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

function testSetAndGetXml (xml xmlContent) (xml) {
    mime:Entity entity = {};
    entity.setXml(xmlContent);
    return entity.getXml();
}

function testGetXmlMultipleTimes (xml xmlContent) (xml) {
    mime:Entity entity = {};
    entity.setXml(xmlContent);
    xml returnContent1 = entity.getXml();
    xml returnContent2 = entity.getXml();
    xml returnContent3 = entity.getXml();
    xml returnContent = returnContent1 + returnContent2 + returnContent3;
    return returnContent;
}

function testSetAndGetText (string textContent) (string) {
    mime:Entity entity = {};
    entity.setText(textContent);
    return entity.getText();
}

function testGetTextMultipleTimes (string textContent) (string) {
    mime:Entity entity = {};
    entity.setText(textContent);
    string returnContent1 = entity.getText();
    string returnContent2 = entity.getText();
    string returnContent3 = entity.getText();
    string returnContent = returnContent1 + returnContent2 + returnContent3;
    return returnContent;
}

function testSetAndGetBlob (blob blobContent) (blob) {
    mime:Entity entity = {};
    entity.setBlob(blobContent);
    return entity.getBlob();
}

function testGetBlobMultipleTimes (blob blobContent) (string) {
    mime:Entity entity = {};
    entity.setBlob(blobContent);
    blob returnContent1 = entity.getBlob();
    blob returnContent2 = entity.getBlob();
    blob returnContent3 = entity.getBlob();
    string contentAsString = returnContent1.toString("utf-8") + returnContent2.toString("utf-8") +
                           returnContent3.toString("utf-8");
    return contentAsString;
}

function testSetFileAsEntityBody (file:File fileHandler) (blob) {
    mime:Entity entity = {};
    entity.setFileAsEntityBody(fileHandler);
    return entity.getBlob();
}

function testSetByteChannel (io:ByteChannel byteChannel) (blob) {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    return entity.getBlob();
}

function testGetByteChannel (io:ByteChannel byteChannel) (io:ByteChannel) {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    return entity.getByteChannel();
}

function testSetEntityBodyMultipleTimes (io:ByteChannel byteChannel, string textdata) (string) {
    mime:Entity entity = {};
    entity.setText(textdata);
    entity.setByteChannel(byteChannel);
    return entity.getText();
}


@http:configuration {basePath:"/test"}
service<http> helloServer {
    @http:resourceConfig {
        methods:["POST"],
        path:"/largepayload"
    }
    resource getPayloadFromFileChannel (http:Connection conn, http:InRequest request) {
        mime:Entity entity = request.getEntity();
        io:ByteChannel byteChannel = entity.getByteChannel();
        http:OutResponse response = {};
        mime:Entity responseEntity = {};
        responseEntity.setByteChannel(byteChannel);
        response.setEntity(responseEntity);
        _ = conn.respond(response);
    }
}

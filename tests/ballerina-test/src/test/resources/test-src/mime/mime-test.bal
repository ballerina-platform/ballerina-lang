import ballerina.mime;
import ballerina.file;
import ballerina.io;
import ballerina.net.http;
import ballerina.net.http.mock;

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

function testSetAndGetJson (json jsonContent) (json, mime:EntityError) {
    mime:Entity entity = {};
    entity.setJson(jsonContent);
    return entity.getJson();
}

function testGetJsonMultipleTimes (json jsonContent) (json) {
    mime:Entity entity = {};
    entity.setJson(jsonContent);
    var returnContent1, entityError1 = entity.getJson();
    var returnContent2, entityError2 = entity.getJson();
    var returnContent3, entityError3 = entity.getJson();
    if (entityError1 != null || entityError2 != null || entityError3 != null) {
       return null;
    }
    json returnContent = {concatContent:[returnContent1, returnContent2, returnContent3]};
    return returnContent;
}

function testSetAndGetXml (xml xmlContent) (xml, mime:EntityError) {
    mime:Entity entity = {};
    entity.setXml(xmlContent);
    return entity.getXml();
}

function testGetXmlMultipleTimes (xml xmlContent) (xml) {
    mime:Entity entity = {};
    entity.setXml(xmlContent);
    var returnContent1, entityError1 = entity.getXml();
    var returnContent2, entityError2 = entity.getXml();
    var returnContent3, entityError3 = entity.getXml();
    if (entityError1 != null && entityError2 != null && entityError3 != null) {
       return null;
    }
    xml returnContent = returnContent1 + returnContent2 + returnContent3;
    return returnContent;
}

function testSetAndGetText (string textContent) (string, mime:EntityError) {
    mime:Entity entity = {};
    entity.setText(textContent);
    return entity.getText();
}

function testGetTextMultipleTimes (string textContent) (string) {
    mime:Entity entity = {};
    entity.setText(textContent);
    var returnContent1, entityError1 = entity.getText();
    var returnContent2, entityError2 = entity.getText();
    var returnContent3, entityError3 = entity.getText();
    if (entityError1 != null && entityError2 != null && entityError3 != null) {
        return null;
    }
    string returnContent = returnContent1 + returnContent2 + returnContent3;
    return returnContent;
}

function testSetAndGetBlob (blob blobContent) (blob, mime:EntityError) {
    mime:Entity entity = {};
    entity.setBlob(blobContent);
    return entity.getBlob();
}

function testGetBlobMultipleTimes (blob blobContent) (string) {
    mime:Entity entity = {};
    entity.setBlob(blobContent);
    var returnContent1, entityError1 = entity.getBlob();
    var returnContent2, entityError2 = entity.getBlob();
    var returnContent3, entityError3 = entity.getBlob();
    if (entityError1 != null && entityError2 != null && entityError3 != null) {
        return null;
    }
    string contentAsString = returnContent1.toString("utf-8") + returnContent2.toString("utf-8") +
                             returnContent3.toString("utf-8");
    return contentAsString;
}

function testSetFileAsEntityBody (file:File fileHandler) (blob, mime:EntityError) {
    mime:Entity entity = {};
    entity.setFileAsEntityBody(fileHandler);
    return entity.getBlob();
}

function testSetByteChannel (io:ByteChannel byteChannel) (blob, mime:EntityError) {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    return entity.getBlob();
}

function testGetByteChannel (io:ByteChannel byteChannel) (io:ByteChannel, mime:EntityError) {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    return entity.getByteChannel();
}

function testSetEntityBodyMultipleTimes (io:ByteChannel byteChannel, string textdata) (string, mime:EntityError) {
    mime:Entity entity = {};
    entity.setText(textdata);
    entity.setByteChannel(byteChannel);
    return entity.getText();
}

function testSetJsonAndGetByteChannel (json jsonContent) (io:ByteChannel, mime:EntityError) {
    mime:Entity entity = {};
    entity.setJson(jsonContent);
    return entity.getByteChannel();
}

function testGetTextDataSource (io:ByteChannel byteChannel) (string, mime:EntityError) {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    //Consume byte channel externally
    var channel, _ = entity.getByteChannel();
    consumeChannel(channel);
    return entity.getText();
}

function testGetJsonDataSource (io:ByteChannel byteChannel) (json, mime:EntityError) {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    //Consume byte channel externally
    var channel, _ = entity.getByteChannel();
    consumeChannel(channel);
    return entity.getJson();
}

function consumeChannel (io:ByteChannel channel) {
    int numberOfBytesRead = 1;
    blob readContent;
    while (numberOfBytesRead != 0) {
        readContent, numberOfBytesRead, _ = channel.read(10000,10000, 0);
    }
}

endpoint<mock:NonListeningService> mockEP {
    port:9090
}

@http:serviceConfig {endpoints:[mockEP]}
service<http:Service> test {
    @http:resourceConfig {
        methods:["POST"],
        path:"/largepayload"
    }
    resource getPayloadFromFileChannel (http:ServerConnector conn, http:Request request) {
        var byteChannel, _ = request.getByteChannel();
        http:Response response = {};
        mime:Entity responseEntity = {};
        responseEntity.setByteChannel(byteChannel);
        response.setEntity(responseEntity);
        _ = conn -> respond(response);
    }
}

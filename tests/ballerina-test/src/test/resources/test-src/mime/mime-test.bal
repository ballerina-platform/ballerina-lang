import ballerina/mime;
import ballerina/log;
import ballerina/file;
import ballerina/io;
import ballerina/http;
import ballerina/http;

function testGetMediaType (string contentType) returns mime:MediaType {
    return mime:getMediaType(contentType);
}

function testToStringOnMediaType (mime:MediaType mediaType) returns (string) {
    return mediaType.toString();
}

function testToStringWithParametersOnMediaType (mime:MediaType mediaType) returns (string) {
    return mediaType.toStringWithParameters();
}

function testMimeBase64Encode (blob value) returns (blob) {
    mime:MimeBase64Encoder encoder = {};
    return encoder.encode(value);
}

function testMimeBase64EncodeString (string content, string charset) returns (string) {
    mime:MimeBase64Encoder encoder = {};
    return encoder.encodeString(content, charset);
}

function testMimeBase64Decode (blob value) returns (blob) {
    mime:MimeBase64Decoder decoder = {};
    return decoder.decode(value);
}

function testMimeBase64DecodeString (string content, string charset) returns (string) {
    mime:MimeBase64Decoder decoder = {};
    return decoder.decodeString(content, charset);
}

function testSetAndGetJson (json jsonContent) returns json | mime:EntityError {
    mime:Entity entity = {};
    entity.setJson(jsonContent);
    return entity.getJson();
}

function testGetJsonMultipleTimes (json jsonContent) returns (json) {
    mime:Entity entity = {};
    entity.setJson(jsonContent);
    json | mime:EntityError returnContent1 = entity.getJson();
    json | mime:EntityError returnContent2 = entity.getJson();
    json | mime:EntityError returnContent3 = entity.getJson();

    json content1 = {};
    json content2 = {};
    json content3 = {};

    match returnContent1 {
        mime:EntityError err => log:printInfo("error in returnContent1");
        json j => { content1 = j;}

     }

    match returnContent2 {
        mime:EntityError err => log:printInfo("error in returnContent2");
        json j => { content2 = j;}
    }

    match returnContent3 {
        mime:EntityError err => log:printInfo("error in returnContent3");
        json j => { content3 = j;}
    }

    json returnContent = {concatContent:[content1, content2, content3]};
    return returnContent;
}

function testSetAndGetXml (xml xmlContent) returns xml | mime:EntityError {
    mime:Entity entity = {};
    entity.setXml(xmlContent);
    return entity.getXml();
}

function testGetXmlMultipleTimes (xml xmlContent) returns (xml) {
    mime:Entity entity = {};
    entity.setXml(xmlContent);
    xml | mime:EntityError returnContent1 = entity.getXml();
    xml | mime:EntityError returnContent2 = entity.getXml();
    xml | mime:EntityError returnContent3 = entity.getXml();

    xml content1;
    xml content2;
    xml content3;

    match returnContent1 {
        mime:EntityError err => log:printInfo("error in returnContent1");
        xml j => { content1 = j;}
    }

    match returnContent2 {
        mime:EntityError err => log:printInfo("error in returnContent2");
        xml j => { content2 = j;}
    }

    match returnContent3 {
        mime:EntityError err => log:printInfo("error in returnContent3");
        xml j => { content3 = j;}
    }

    xml returnContent = content1 + content2 + content3;
    return returnContent;
}

function testSetAndGetText (string textContent) returns string | null | mime:EntityError {
    mime:Entity entity = {};
    entity.setText(textContent);
    return entity.getText();
}

function testGetTextMultipleTimes (string textContent) returns (string) {
    mime:Entity entity = {};
    entity.setText(textContent);
    string | mime:EntityError | null returnContent1 = entity.getText();
    string | mime:EntityError | null returnContent2 = entity.getText();
    string | mime:EntityError | null returnContent3 = entity.getText();

    string content1;
    string content2;
    string content3;

    match returnContent1 {
        int | null => {log:printInfo("null");}
        mime:EntityError err => log:printInfo("error in returnContent1");
        string j => { content1 = j;}
    }

    match returnContent2 {
        int | null => {log:printInfo("null");}
        mime:EntityError err => log:printInfo("error in returnContent2");
        string j => { content2 = j;}
    }

    match returnContent3 {
        int | null => {log:printInfo("null");}
        mime:EntityError err => log:printInfo("error in returnContent3");
        string j => { content3 = j;}
    }
    string returnContent = content1 + content2 + content3;
    return returnContent;
}

function testSetAndGetBlob (blob blobContent) returns blob | mime:EntityError {
    mime:Entity entity = {};
    entity.setBlob(blobContent);
    return entity.getBlob();
}

function testGetBlobMultipleTimes (blob blobContent) returns (string) {
    mime:Entity entity = {};
    entity.setBlob(blobContent);
    blob | mime:EntityError returnContent1 = entity.getBlob();
    blob | mime:EntityError returnContent2 = entity.getBlob();
    blob | mime:EntityError returnContent3 = entity.getBlob();

    blob content1;
    blob content2;
    blob content3;

    match returnContent1 {
        mime:EntityError err => log:printInfo("error in returnContent1");
        blob j => { content1 = j;}
    }

    match returnContent2 {
        mime:EntityError err => log:printInfo("error in returnContent2");
        blob j => { content2 = j;}
    }

    match returnContent3 {
        mime:EntityError err => log:printInfo("error in returnContent3");
        blob j => { content3 = j;}
    }


    string contentAsString = content1.toString("utf-8") + content2.toString("utf-8") +
                             content3.toString("utf-8");
    return contentAsString;
}

function testSetFileAsEntityBody (file:File fileHandler) returns blob | mime:EntityError {
    mime:Entity entity = {};
    entity.setFileAsEntityBody(fileHandler);
    return entity.getBlob();
}

function testSetByteChannel (io:ByteChannel byteChannel) returns blob | mime:EntityError {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    return entity.getBlob();
}

function testGetByteChannel (io:ByteChannel byteChannel) returns io:ByteChannel | mime:EntityError {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    return entity.getByteChannel();
}

function testSetEntityBodyMultipleTimes (io:ByteChannel byteChannel, string textdata) returns string | null | mime:EntityError {
    mime:Entity entity = {};
    entity.setText(textdata);
    entity.setByteChannel(byteChannel);
    return entity.getText();
}

function testSetJsonAndGetByteChannel (json jsonContent) returns io:ByteChannel | mime:EntityError {
    mime:Entity entity = {};
    entity.setJson(jsonContent);
    return entity.getByteChannel();
}

function testGetTextDataSource (io:ByteChannel byteChannel) returns string | mime:EntityError {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    //Consume byte channel externally
    var result = entity.getByteChannel();
    match result {
        mime:EntityError err => log:printInfo("error in returnContent1");
        io:ByteChannel j => { consumeChannel(j);}
    }
    return entity.getText();
}

function testGetJsonDataSource (io:ByteChannel byteChannel) returns json | mime:EntityError {
    mime:Entity entity = {};
    entity.setByteChannel(byteChannel);
    //Consume byte channel externally
    var result = entity.getByteChannel();
    match result {
        mime:EntityError err => log:printInfo("error in returnContent1");
        io:ByteChannel j => { consumeChannel(j);}
    }

    return entity.getJson();
}

function consumeChannel (io:ByteChannel channel) {
    var result = channel.read(1000000);
}

endpoint http:NonListeningServiceEndpoint mockEP {
    port:9090
};

@http:ServiceConfig {basePath:"/test"}
service<http:Service> echo bind mockEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/largepayload"
    }
    getPayloadFromFileChannel (endpoint client, http:Request request) {
        http:Response response = {};
        mime:Entity responseEntity = {};
        match request.getByteChannel() {
            http:PayloadError err => log:printInfo("invalid value");
            io:ByteChannel byteChannel => responseEntity.setByteChannel(byteChannel);
        }
        response.setEntity(responseEntity);
        _ = client -> respond(response);
    }
}

import ballerina/mime;
import ballerina/log;
import ballerina/file;
import ballerina/io;
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

function testMimeBase64EncodeString (string contentToBeEncoded) returns (string | mime:Base64EncodeError) {
    return mime:base64EncodeString(contentToBeEncoded);
}

function testMimeBase64DecodeString (string contentToBeDecoded) returns (string | mime:Base64DecodeError) {
    return mime:base64DecodeString(contentToBeDecoded);
}

function testMimeBase64EncodeBlob (blob contentToBeEncoded) returns (blob | mime:Base64EncodeError) {
    return mime:base64EncodeBlob(contentToBeEncoded);
}

function testMimeBase64DecodeBlob (blob contentToBeDecoded) returns (blob | mime:Base64DecodeError) {
    return mime:base64DecodeBlob(contentToBeDecoded);
}

function testMimeBase64EncodeByteChannel (io:ByteChannel contentToBeEncoded) returns (io:ByteChannel  | mime:Base64EncodeError) {
    return mime:base64EncodeByteChannel(contentToBeEncoded);
}

function testMimeBase64DecodeByteChannel (io:ByteChannel contentToBeDecoded) returns (io:ByteChannel  | mime:Base64DecodeError) {
    return mime:base64DecodeByteChannel(contentToBeDecoded);
}

function testSetAndGetJson (json jsonContent) returns json | mime:EntityError {
    mime:Entity entity = new;
    entity.setJson(jsonContent);
    return entity.getJson();
}

function testGetJsonMultipleTimes (json jsonContent) returns (json) {
    mime:Entity entity = new;
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
    mime:Entity entity = new;
    entity.setXml(xmlContent);
    return entity.getXml();
}

function testGetXmlMultipleTimes (xml xmlContent) returns (xml) {
    mime:Entity entity = new;
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

function testSetAndGetText (string textContent) returns string | mime:EntityError {
    mime:Entity entity = new;
    entity.setText(textContent);
    return entity.getText();
}

function testGetTextMultipleTimes (string textContent) returns (string) {
    mime:Entity entity = new;
    entity.setText(textContent);
    string | mime:EntityError returnContent1 = entity.getText();
    string | mime:EntityError returnContent2 = entity.getText();
    string | mime:EntityError returnContent3 = entity.getText();

    string content1;
    string content2;
    string content3;

    match returnContent1 {
        mime:EntityError err => log:printInfo("error in returnContent1");
        string j => { content1 = j;}
    }

    match returnContent2 {
        mime:EntityError err => log:printInfo("error in returnContent2");
        string j => { content2 = j;}
    }

    match returnContent3 {
        mime:EntityError err => log:printInfo("error in returnContent3");
        string j => { content3 = j;}
    }
    string returnContent = content1 + content2 + content3;
    return returnContent;
}

function testSetAndGetBlob (blob blobContent) returns blob | mime:EntityError {
    mime:Entity entity = new;
    entity.setBlob(blobContent);
    return entity.getBlob();
}

function testGetBlobMultipleTimes (blob blobContent) returns (string) {
    mime:Entity entity = new;
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

function testSetFileAsEntityBody (string fileLocation) returns blob | mime:EntityError {
    mime:Entity entity = new;
    file:Path path = file:getPath(fileLocation);
    entity.setFileAsEntityBody(path);
    return entity.getBlob();
}

function testSetByteChannel (io:ByteChannel byteChannel) returns blob | mime:EntityError {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel);
    return entity.getBlob();
}

function testGetByteChannel (io:ByteChannel byteChannel) returns io:ByteChannel | mime:EntityError {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel);
    return entity.getByteChannel();
}

function testSetEntityBodyMultipleTimes (io:ByteChannel byteChannel, string textdata) returns string | mime:EntityError {
    mime:Entity entity = new;
    entity.setText(textdata);
    entity.setByteChannel(byteChannel);
    return entity.getText();
}

function testSetJsonAndGetByteChannel (json jsonContent) returns io:ByteChannel | mime:EntityError {
    mime:Entity entity = new;
    entity.setJson(jsonContent);
    return entity.getByteChannel();
}

function testGetTextDataSource (io:ByteChannel byteChannel) returns string | mime:EntityError {
    mime:Entity entity = new;
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
    mime:Entity entity = new;
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
        http:Response response = new;
        mime:Entity responseEntity = new;
        match request.getByteChannel() {
            http:PayloadError err => log:printInfo("invalid value");
            io:ByteChannel byteChannel => responseEntity.setByteChannel(byteChannel);
        }
        response.setEntity(responseEntity);
        _ = client -> respond(response);
    }
}

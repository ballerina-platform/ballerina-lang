import ballerina/mime;
import ballerina/log;
import ballerina/file;
import ballerina/io;

function getMediaTypeTestObj() returns mime:MediaType {
    mime:MediaType mediaType = new;
    mediaType.primaryType = "application";
    mediaType.subType = "my-custom-type+json";
    return mediaType;
}

function getDispositionTestObj() returns mime:ContentDisposition {
    mime:ContentDisposition disposition = new;
    disposition.fileName = "test_file.xml";
    disposition.disposition = "inline";
    disposition.name = "test";
    return disposition;
}

//Media type tests
function testGetMediaType(string contentType) returns mime:MediaType|error {
    return mime:getMediaType(contentType);
}

function testGetBaseTypeOnMediaType(mime:MediaType mediaType) returns (string) {
    return mediaType.getBaseType();
}

function testToStringOnMediaType(mime:MediaType mediaType) returns (string) {
    return mediaType.toString();
}

function testGetContentDispositionObject(string contentDisposition) returns mime:ContentDisposition {
    return mime:getContentDispositionObject(contentDisposition);
}

function testToStringOnContentDisposition(mime:ContentDisposition contentDisposition) returns (string) {
    return contentDisposition.toString();
}

function testSetMediaTypeToEntity() returns string? {
    mime:Entity entity = new;
    mime:MediaType mediaType = getMediaTypeTestObj();
    entity.setContentType(mediaType.toString());
    return entity.getContentType();
}

function testSetMediaTypeAndGetValueAsHeader() returns string {
    mime:Entity entity = new;
    mime:MediaType mediaType = getMediaTypeTestObj();
    entity.setContentType(mediaType.toString());
    return entity.getHeader(mime:CONTENT_TYPE);
}

function testSetHeaderAndGetMediaType() returns string {
    mime:Entity entity = new;
    entity.setHeader(mime:CONTENT_TYPE, "text/plain; charset=UTF-8");
    return entity.getContentType();
}

//Content-disposition tests
function testSetContentDispositionToEntity() returns string {
    mime:Entity entity = new;
    entity.setContentDisposition(getDispositionTestObj());
    mime:ContentDisposition disposition = entity.getContentDisposition();
    return disposition.toString();
}

function testSetContentDispositionAndGetValueAsHeader() returns string {
    mime:Entity entity = new;
    entity.setContentDisposition(getDispositionTestObj());
    return entity.getHeader(mime:CONTENT_DISPOSITION);
}

function testSetHeaderAndGetContentDisposition() returns string {
    mime:Entity entity = new;
    entity.setHeader(mime:CONTENT_DISPOSITION, "inline;name=\"test\";filename=\"test_file.xml\"");
    mime:ContentDisposition receivedDisposition = entity.getContentDisposition();
    return receivedDisposition.toString();
}

//Content-length tests
function testSetContentLengthToEntity() returns int|error {
    mime:Entity entity = new;
    entity.setContentLength(45555);
    return entity.getContentLength();
}

function testSetContentLengthAndGetValueAsHeader() returns string {
    mime:Entity entity = new;
    entity.setContentLength(45555);
    return entity.getHeader(mime:CONTENT_LENGTH);
}

//Content-id tests
function testSetContentIdToEntity() returns string {
    mime:Entity entity = new;
    entity.setContentId("test-id");
    return entity.getContentId();
}

function testSetContentIdAndGetValueAsHeader() returns string {
    mime:Entity entity = new;
    entity.setContentId("test-id");
    return entity.getHeader(mime:CONTENT_ID);
}

function testMimeBase64EncodeString(string contentToBeEncoded) returns (string|error) {
    return mime:base64EncodeString(contentToBeEncoded);
}

function testMimeBase64DecodeString(string contentToBeDecoded) returns (string|error) {
    return mime:base64DecodeString(contentToBeDecoded);
}

function testMimeBase64EncodeBlob(byte[] contentToBeEncoded) returns (byte[]|error) {
    return mime:base64EncodeBlob(contentToBeEncoded);
}

function testMimeBase64DecodeBlob(byte[] contentToBeDecoded) returns (byte[]|error) {
    return mime:base64DecodeBlob(contentToBeDecoded);
}

function testMimeBase64EncodeByteChannel(io:ByteChannel contentToBeEncoded) returns (io:ByteChannel|error) {
    return mime:base64EncodeByteChannel(contentToBeEncoded);
}

function testMimeBase64DecodeByteChannel(io:ByteChannel contentToBeDecoded) returns (io:ByteChannel|error) {
    return mime:base64DecodeByteChannel(contentToBeDecoded);
}

function testSetAndGetJson(json jsonContent) returns json|error {
    mime:Entity entity = new;
    entity.setJson(jsonContent);
    return entity.getJson();
}

function testGetJsonMultipleTimes(json jsonContent) returns (json) {
    mime:Entity entity = new;
    entity.setJson(jsonContent);
    json|error returnContent1 = entity.getJson();
    json|error returnContent2 = entity.getJson();
    json|error returnContent3 = entity.getJson();

    json content1 = {};
    json content2 = {};
    json content3 = {};

    match returnContent1 {
        error err => log:printInfo("error in returnContent1");
        json j => {
            content1 = j;
        }

    }

    match returnContent2 {
        error err => log:printInfo("error in returnContent2");
        json j => {
            content2 = j;
        }
    }

    match returnContent3 {
        error err => log:printInfo("error in returnContent3");
        json j => {
            content3 = j;
        }
    }

    json returnContent = { concatContent: [content1, content2, content3] };
    return returnContent;
}

function testSetAndGetXml(xml xmlContent) returns xml|error {
    mime:Entity entity = new;
    entity.setXml(xmlContent);
    return entity.getXml();
}

function testGetXmlMultipleTimes(xml xmlContent) returns (xml) {
    mime:Entity entity = new;
    entity.setXml(xmlContent);
    xml|error returnContent1 = entity.getXml();
    xml|error returnContent2 = entity.getXml();
    xml|error returnContent3 = entity.getXml();

    xml content1;
    xml content2;
    xml content3;

    match returnContent1 {
        error err => log:printInfo("error in returnContent1");
        xml j => {
            content1 = j;
        }
    }

    match returnContent2 {
        error err => log:printInfo("error in returnContent2");
        xml j => {
            content2 = j;
        }
    }

    match returnContent3 {
        error err => log:printInfo("error in returnContent3");
        xml j => {
            content3 = j;
        }
    }

    xml returnContent = content1 + content2 + content3;
    return returnContent;
}

function testSetAndGetText(string textContent) returns string|error {
    mime:Entity entity = new;
    entity.setText(textContent);
    return entity.getText();
}

function testGetTextMultipleTimes(string textContent) returns (string) {
    mime:Entity entity = new;
    entity.setText(textContent);
    string|error returnContent1 = entity.getText();
    string|error returnContent2 = entity.getText();
    string|error returnContent3 = entity.getText();

    string content1;
    string content2;
    string content3;

    match returnContent1 {
        error err => log:printInfo("error in returnContent1");
        string textBody => {
            content1 = textBody;
        }
    }

    match returnContent2 {
        error err => log:printInfo("error in returnContent2");
        string textBody => {
            content2 = textBody;
        }
    }

    match returnContent3 {
        error err => log:printInfo("error in returnContent3");
        string textBody => {
            content3 = textBody;
        }
    }
    string returnContent = content1 + content2 + content3;
    return returnContent;
}

function testSetAndGetByteArray(byte[] blobContent) returns byte[]|error {
    mime:Entity entity = new;
    entity.setByteArray(blobContent);
    return entity.getByteArray();
}

function testGetByteArrayMultipleTimes(byte[] blobContent) returns (string) {
    mime:Entity entity = new;
    entity.setByteArray(blobContent);
    byte[]|error returnContent1 = entity.getByteArray();
    byte[]|error returnContent2 = entity.getByteArray();
    byte[]|error returnContent3 = entity.getByteArray();

    byte[] content1;
    byte[] content2;
    byte[] content3;

    match returnContent1 {
        error err => log:printInfo("error in returnContent1");
        byte[] binaryBody => {
            content1 = binaryBody;
        }
    }

    match returnContent2 {
        error err => log:printInfo("error in returnContent2");
        byte[] binaryBody => {
            content2 = binaryBody;
        }
    }

    match returnContent3 {
        error err => log:printInfo("error in returnContent3");
        byte[] binaryBody => {
            content3 = binaryBody;
        }
    }


    string contentAsString = mime:byteArrayToString(content1, "utf-8") + mime:byteArrayToString(content2, "utf-8") +
        mime:byteArrayToString(content3, "utf-8");
    return contentAsString;
}

function testSetFileAsEntityBody(string fileLocation) returns byte[]|error {
    mime:Entity entity = new;
    entity.setFileAsEntityBody(fileLocation);
    return entity.getByteArray();
}

function testSetByteChannel(io:ByteChannel byteChannel) returns byte[]|error {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel);
    return entity.getByteArray();
}

function testGetByteChannel(io:ByteChannel byteChannel) returns io:ByteChannel|error {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel);
    return entity.getByteChannel();
}

function testSetEntityBodyMultipleTimes(io:ByteChannel byteChannel, string textdata) returns string {
    mime:Entity entity = new;
    entity.setText(textdata);
    entity.setByteChannel(byteChannel);
    io:ByteChannel receivedByteChannel = check entity.getByteChannel();
    io:CharacterChannel characterChannel = new io:CharacterChannel(receivedByteChannel, "utf-8");
    string result = check characterChannel.read(30);
    return result;
}

function testSetJsonAndGetByteChannel(json jsonContent) returns io:ByteChannel|error {
    mime:Entity entity = new;
    entity.setJson(jsonContent);
    return entity.getByteChannel();
}

function testGetTextDataSource(io:ByteChannel byteChannel) returns string|error {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel);
    entity.setHeader("content-type", "text/plain");
    //Consume byte channel externally
    var result = entity.getByteChannel();
    match result {
        error err => log:printInfo("error in returnContent1");
        io:ByteChannel returnChannel => {
            consumeChannel(returnChannel);
        }
    }
    return entity.getText();
}

function testGetJsonDataSource(io:ByteChannel byteChannel) returns json|error {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel);
    entity.setHeader("content-type", "application/json");
    //Consume byte channel externally
    var result = entity.getByteChannel();
    match result {
        error err => log:printInfo("error in returnContent1");
        io:ByteChannel returnChannel => {
            consumeChannel(returnChannel);
        }
    }

    return entity.getJson();
}

function consumeChannel(io:ByteChannel channel) {
    var result = channel.read(1000000);
}

function testGetXmlWithSuffix(xml xmlContent) returns xml|error {
    mime:Entity entity = new;
    entity.setHeader("content-type", "application/3gpdash-qoe-report+xml");
    entity.setXml(xmlContent);
    return entity.getXml();
}

function testGetXmlWithNonCompatibleMediaType(xml xmlContent) returns xml|error {
    mime:Entity entity = new;
    entity.setXml(xmlContent);
    entity.setHeader("content-type", "application/3gpdash-qoe-report");
    return entity.getXml();
}

function testGetJsonWithSuffix(json jsonContent) returns json|error {
    mime:Entity entity = new;
    entity.setHeader("content-type", "application/yang-patch+json");
    entity.setJson(jsonContent);
    return entity.getJson();
}

function testGetJsonWithNonCompatibleMediaType(json jsonContent) returns json|error {
    mime:Entity entity = new;
    entity.setJson(jsonContent);
    entity.setHeader("content-type", "application/whoispp-query");
    return entity.getJson();
}

function testGetTextWithNonCompatibleMediaType(string textContent) returns string|error {
    mime:Entity entity = new;
    entity.setText(textContent);
    entity.setHeader("content-type", "model/vnd.parasolid.transmit");
    return entity.getText();
}

function testSetBodyAndGetText((string|xml|json|byte[]|io:ByteChannel) entityBody) returns string|error {
    mime:Entity entity = new;
    entity.setBody(entityBody);
    return entity.getText();
}

function testSetBodyAndGetXml((string|xml|json|byte[]|io:ByteChannel) entityBody) returns xml|error {
    mime:Entity entity = new;
    entity.setBody(entityBody);
    return entity.getXml();
}

function testSetBodyAndGetJson((string|xml|json|byte[]|io:ByteChannel) entityBody) returns json|error {
    mime:Entity entity = new;
    entity.setBody(entityBody);
    return entity.getJson();
}

function testSetBodyAndGetByteArray((string|xml|json|byte[]|io:ByteChannel) entityBody) returns byte[]|error {
    mime:Entity entity = new;
    entity.setBody(entityBody);
    return entity.getByteArray();
}

function testSetBodyAndGetByteChannel((string|xml|json|byte[]|io:ByteChannel) entityBody) returns io:ByteChannel|error {
    mime:Entity entity = new;
    entity.setBody(entityBody);
    return entity.getByteChannel();
}

function testGetAnyStreamAsString(io:ByteChannel byteChannel, string contentType) returns string|error {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel, contentType = contentType);
    return entity.getBodyAsString();
}

function testByteArrayWithContentType(io:ByteChannel byteChannel, string contentTypeValue) returns byte[]|error {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel, contentType = contentTypeValue);
    //First time the json will be constructed from the byte channel
    json firstTime = check entity.getJson();
    //Then get the body as byte[]
    return entity.getByteArray();
}

function testGetBodyPartsAsChannel() returns io:ByteChannel|error {
    //Create a body part with json content.
    mime:Entity bodyPart1 = new;
    bodyPart1.setJson({ "bodyPart": "jsonPart" });

    //Create another body part with a xml file.
    mime:Entity bodyPart2 = new;
    bodyPart2.setFileAsEntityBody("src/test/resources/datafiles/mime/file.xml", contentType = mime:TEXT_XML);

    //Create a text body part.
    mime:Entity bodyPart3 = new;
    bodyPart3.setText("Ballerina text body part");

    //Create another body part with a text file.
    mime:Entity bodyPart4 = new;
    bodyPart4.setFileAsEntityBody("src/test/resources/datafiles/mime/test.tmp");

    //Create an array to hold all the body parts.
    mime:Entity[] bodyParts = [bodyPart1, bodyPart2, bodyPart3, bodyPart4];
    mime:Entity multipartEntity = new;
    string contentType = mime:MULTIPART_MIXED + "; boundary=e3a0b9ad7b4e7cdt";
    multipartEntity.setBodyParts(bodyParts, contentType = contentType);

    return multipartEntity.getBodyPartsAsChannel();
}

function getBodyPartsFromDiscreteTypeEntity() returns mime:Entity[]|error {
    mime:Entity entity = new;
    entity.setJson({ "bodyPart": "jsonPart" });
    return entity.getBodyParts();
}

function getChannelFromParts() returns io:ByteChannel|error {
    mime:Entity entity = new;
    entity.setJson({ "bodyPart": "jsonPart" });
    return entity.getBodyPartsAsChannel();
}

function getChannelFromMultipartEntity() returns io:ByteChannel|error {
    mime:Entity bodyPart1 = new;
    bodyPart1.setJson({ "bodyPart": "jsonPart" });

    mime:Entity bodyPart2 = new;
    bodyPart2.setText("Ballerina text body part");

    mime:Entity[] bodyParts = [bodyPart1, bodyPart2];
    mime:Entity multipartEntity = new;
    multipartEntity.setBodyParts(bodyParts);

    return multipartEntity.getByteChannel();
}

function getAnyStreamAsStringFromCache(io:ByteChannel byteChannel, string contentType) returns string|error {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel, contentType = contentType);
    string returnContent;
    returnContent = check entity.getBodyAsString();
    //String body should be retrieved from the cache the second time this is called
    returnContent = returnContent + check entity.getBodyAsString();
    return returnContent;
}

function testXmlWithByteArrayContent(io:ByteChannel byteChannel, string contentTypeValue) returns xml|error {
    mime:Entity entity = new;
    entity.setByteChannel(byteChannel, contentType = contentTypeValue);
    byte[] binaryPayload = check entity.getByteArray();
    return entity.getXml();
}

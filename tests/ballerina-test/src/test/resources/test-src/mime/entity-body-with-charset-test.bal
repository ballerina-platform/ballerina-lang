import ballerina/mime;
import ballerina/net.http;

string textValue ="Hello Ballerina!";
xml testValue = xml `<test><name>ballerina</name></test>`;

//Request charset with json payload
function testSetJsonPayloadWithCharset (string charset) returns (string[]) {
    http:Request request = {};
    request.setJsonPayload({test:"testValue"}, charset=charset);
    return request.getHeaders("content-type");
}

function testSetJsonPayloadWithoutCharset () returns (string[]) {
    http:Request request = {};
    request.setJsonPayload({test:"testValue"});
    return request.getHeaders("content-type");
}

function testCharsetWithExistingContentType (string charset) returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "application/json;charset=\"ISO_8859-1:1987\"");
    request.setJsonPayload({test:"testValue"}, charset=charset);
    return request.getHeaders("content-type");
}

function testNoCharsetWithExistingContentType () returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "application/json;charset=UNICODE-1-1-UTF-7");
    request.setJsonPayload({test:"testValue"});
    return request.getHeaders("content-type");
}

function testSetHeaderAfterJsonPayload (string charset) returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "application/json;charset=utf-8");
    request.setJsonPayload({test:"testValue"}, charset=charset);
    request.setHeader("content-type", "application/json;charset=\"ISO_8859-1:1987\"");
    return request.getHeaders("content-type");
}

//Request charset with xml payload
function testSetXmlPayloadWithCharset (string charset) returns (string[]) {
    http:Request request = {};
    request.setXmlPayload(testValue, charset=charset);
    return request.getHeaders("content-type");
}

function testSetXmlPayloadWithoutCharset () returns (string[]) {
    http:Request request = {};
    request.setXmlPayload(testValue);
    return request.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeXml (string charset) returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "application/xml;charset=\"ISO_8859-1:1987\"");
    request.setXmlPayload(testValue, charset=charset);
    return request.getHeaders("content-type");
}

function testNoCharsetWithExistingContentTypeXml () returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "application/xml;charset=UNICODE-1-1-UTF-7");
    request.setXmlPayload(testValue);
    return request.getHeaders("content-type");
}

function testSetHeaderAfterXmlPayload (string charset) returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "application/xml;charset=utf-8");
    request.setXmlPayload(testValue, charset=charset);
    request.setHeader("content-type", "application/xml;charset=\"ISO_8859-1:1987\"");
    return request.getHeaders("content-type");
}

//Request charset with string payload

function testSetStringPayloadWithCharset (string charset) returns (string[]) {
    http:Request request = {};
    request.setStringPayload(textValue, charset=charset);
    return request.getHeaders("content-type");
}

function testSetStringPayloadWithoutCharset () returns (string[]) {
    http:Request request = {};
    request.setStringPayload(textValue);
    return request.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeString (string charset) returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "text/plain;charset=\"ISO_8859-1:1987\"");
    request.setStringPayload(textValue, charset=charset);
    return request.getHeaders("content-type");
}

function testNoCharsetWithExistingContentTypeString () returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "text/plain;charset=UNICODE-1-1-UTF-7");
    request.setStringPayload(textValue);
    return request.getHeaders("content-type");
}

function testSetHeaderAfterStringPayload (string charset) returns (string[]) {
    http:Request request = {};
    request.setHeader("content-type", "text/plain;charset=utf-8");
    request.setStringPayload(textValue, charset=charset);
    request.setHeader("content-type", "text/plain;charset=\"ISO_8859-1:1987\"");
    return request.getHeaders("content-type");
}

//Response charset with json payload

function testSetJsonPayloadWithCharsetResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setJsonPayload({test:"testValue"}, charset=charset);
    return response.getHeaders("content-type");
}

function testSetJsonPayloadWithoutCharsetResponse () returns (string[]) {
    http:Response response = {};
    response.setJsonPayload({test:"testValue"});
    return response.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "application/json;charset=\"ISO_8859-1:1987\"");
    response.setJsonPayload({test:"testValue"}, charset=charset);
    return response.getHeaders("content-type");
}

function testNoCharsetWithExistingContentTypeResponse () returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "application/json;charset=UNICODE-1-1-UTF-7");
    response.setJsonPayload({test:"testValue"});
    return response.getHeaders("content-type");
}

function testSetHeaderAfterJsonPayloadResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "application/json;charset=utf-8");
    response.setJsonPayload({test:"testValue"}, charset=charset);
    response.setHeader("content-type", "application/json;charset=\"ISO_8859-1:1987\"");
    return response.getHeaders("content-type");
}

//Response charset with xml payload
function testSetXmlPayloadWithCharsetResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setXmlPayload(testValue, charset=charset);
    return response.getHeaders("content-type");
}

function testSetXmlPayloadWithoutCharsetResponse () returns (string[]) {
    http:Response response = {};
    response.setXmlPayload(testValue);
    return response.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeXmlResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "application/xml;charset=\"ISO_8859-1:1987\"");
    response.setXmlPayload(testValue, charset=charset);
    return response.getHeaders("content-type");
}

function testNoCharsetWithExistingContentTypeXmlResponse () returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "application/xml;charset=UNICODE-1-1-UTF-7");
    response.setXmlPayload(testValue);
    return response.getHeaders("content-type");
}

function testSetHeaderAfterXmlPayloadResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "application/xml;charset=utf-8");
    response.setXmlPayload(testValue, charset=charset);
    response.setHeader("content-type", "application/xml;charset=\"ISO_8859-1:1987\"");
    return response.getHeaders("content-type");
}

//Response charset with string payload

function testSetStringPayloadWithCharsetResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setStringPayload(textValue, charset=charset);
    return response.getHeaders("content-type");
}

function testSetStringPayloadWithoutCharsetResponse () returns (string[]) {
    http:Response response = {};
    response.setStringPayload(textValue);
    return response.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeStringResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "text/plain;charset=\"ISO_8859-1:1987\"");
    response.setStringPayload(textValue, charset=charset);
    return response.getHeaders("content-type");
}

function testNoCharsetWithExistingContentTypeStringResponse () returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "text/plain;charset=UNICODE-1-1-UTF-7");
    response.setStringPayload(textValue);
    return response.getHeaders("content-type");
}

function testSetHeaderAfterStringPayloadResponse (string charset) returns (string[]) {
    http:Response response = {};
    response.setHeader("content-type", "text/plain;charset=utf-8");
    response.setStringPayload(textValue, charset=charset);
    response.setHeader("content-type", "text/plain;charset=\"ISO_8859-1:1987\"");
    return response.getHeaders("content-type");
}

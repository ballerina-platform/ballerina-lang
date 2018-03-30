import ballerina/mime;
import ballerina/net.http;

//Request Content-Type for json payload
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

//Response Content-Type for json payload

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


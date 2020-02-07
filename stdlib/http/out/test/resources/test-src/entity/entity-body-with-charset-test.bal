import ballerina/http;

string textValue = "Hello Ballerina!";
xml testValue = xml `<test><name>ballerina</name></test>`;
xml xmlValue = xml `<菜鸟驿站><name>菜鸟驿站</name></菜鸟驿站>`;

//Request charset with json payload
function testSetJsonPayloadWithoutCharset() returns @tainted string[] {
    http:Request request = new;
    request.setJsonPayload({ test: "testValue" });
    return request.getHeaders("content-type");
}

function testCharsetWithExistingContentType() returns @tainted string[] {
    http:Request request = new;
    request.setJsonPayload({ test: "testValue" }, "application/json;charset=\"ISO_8859-1:1987\"");
    return request.getHeaders("content-type");
}

function testSetHeaderAfterJsonPayload() returns @tainted string[] {
    http:Request request = new;
    request.setHeader("content-type", "application/json;charset=utf-8");
    request.setJsonPayload({ test: "testValue" });
    request.setHeader("content-type", "application/json;charset=\"ISO_8859-1:1987\"");
    return request.getHeaders("content-type");
}

function testJsonPayloadWithDefaultCharset() returns @tainted json|error {
    http:Request request = new;
    request.setJsonPayload({ test: "菜鸟驿站" });
    return request.getJsonPayload();
}

function testJsonPayloadWithCharset() returns @tainted json|error {
    http:Request request = new;
    request.setJsonPayload({ test: "ߢߚߟ" }, "application/json;charset=utf-8");
    return request.getJsonPayload();
}

//Request charset with xml payload
function testSetXmlPayloadWithoutCharset() returns @tainted string[] {
    http:Request request = new;
    request.setXmlPayload(testValue);
    return request.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeXml() returns @tainted string[] {
    http:Request request = new;
    request.setHeader("content-type", "application/xml;charset=\"ISO_8859-1:1987\"");
    request.setXmlPayload(testValue);
    return request.getHeaders("content-type");
}

function testSetHeaderAfterXmlPayload() returns @tainted string[] {
    http:Request request = new;
    request.setHeader("content-type", "application/xml;charset=utf-8");
    request.setXmlPayload(testValue);
    request.setHeader("content-type", "application/xml;charset=\"ISO_8859-1:1987\"");
    return request.getHeaders("content-type");
}

function testXmlPayloadWithDefaultCharset() returns @tainted xml|error {
    http:Request request = new;
    request.setXmlPayload(xmlValue);
    return request.getXmlPayload();
}

function testXmlPayloadWithCharset() returns @tainted xml|error {
    http:Request request = new;
    request.setXmlPayload(xmlValue, "application/xml;charset=utf-8");
    return request.getXmlPayload();
}

//Request charset with string payload
function testSetStringPayloadWithoutCharset() returns @tainted string[] {
    http:Request request = new;
    request.setTextPayload(textValue);
    return request.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeString() returns @tainted string[] {
    http:Request request = new;
    request.setHeader("content-type", "text/plain;charset=\"ISO_8859-1:1987\"");
    request.setTextPayload(textValue);
    return request.getHeaders("content-type");
}

function testSetHeaderAfterStringPayload() returns @tainted string[] {
    http:Request request = new;
    request.setHeader("content-type", "text/plain;charset=utf-8");
    request.setTextPayload(textValue);
    request.setHeader("content-type", "text/plain;charset=\"ISO_8859-1:1987\"");
    return request.getHeaders("content-type");
}

function testTextPayloadWithDefaultCharset() returns @tainted string|error {
    http:Request request = new;
    request.setTextPayload("菜鸟驿站");
    return request.getTextPayload();
}

function testTextPayloadWithCharset() returns @tainted string|error {
    http:Request request = new;
    request.setTextPayload("菜鸟驿站", "text/plain;charset=utf-8");
    return request.getTextPayload();
}

//Response charset with json payload
function testSetJsonPayloadWithoutCharsetResponse() returns @tainted string[] {
    http:Response response = new;
    response.setJsonPayload({ test: "testValue" });
    return response.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeResponse() returns @tainted string[] {
    http:Response response = new;
    response.setHeader("content-type", "application/json;charset=\"ISO_8859-1:1987\"");
    response.setJsonPayload({ test: "testValue" });
    return response.getHeaders("content-type");
}

function testSetHeaderAfterJsonPayloadResponse() returns @tainted string[] {
    http:Response response = new;
    response.setHeader("content-type", "application/json;charset=utf-8");
    response.setJsonPayload({ test: "testValue" });
    response.setHeader("content-type", "application/json;charset=\"ISO_8859-1:1987\"");
    return response.getHeaders("content-type");
}

//Response charset with xml payload
function testSetXmlPayloadWithoutCharsetResponse() returns @tainted string[] {
    http:Response response = new;
    response.setXmlPayload(testValue);
    return response.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeXmlResponse() returns @tainted string[] {
    http:Response response = new;
    response.setHeader("content-type", "application/xml;charset=\"ISO_8859-1:1987\"");
    response.setXmlPayload(testValue);
    return response.getHeaders("content-type");
}

function testSetHeaderAfterXmlPayloadResponse() returns @tainted string[] {
    http:Response response = new;
    response.setHeader("content-type", "application/xml;charset=utf-8");
    response.setXmlPayload(testValue);
    response.setHeader("content-type", "application/xml;charset=\"ISO_8859-1:1987\"");
    return response.getHeaders("content-type");
}

//Response charset with string payload
function testSetStringPayloadWithoutCharsetResponse() returns @tainted string[] {
    http:Response response = new;
    response.setTextPayload(textValue);
    return response.getHeaders("content-type");
}

function testCharsetWithExistingContentTypeStringResponse() returns @tainted string[] {
    http:Response response = new;
    response.setHeader("content-type", "text/plain;charset=\"ISO_8859-1:1987\"");
    response.setTextPayload(textValue);
    return response.getHeaders("content-type");
}

function testSetHeaderAfterStringPayloadResponse() returns @tainted string[] {
    http:Response response = new;
    response.setHeader("content-type", "text/plain;charset=utf-8");
    response.setTextPayload(textValue);
    response.setHeader("content-type", "text/plain;charset=\"ISO_8859-1:1987\"");
    return response.getHeaders("content-type");
}

listener http:MockListener mockEP = new(9090);

@http:ServiceConfig { basePath: "/test" }
service echo on mockEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/jsonTest"
    }
    resource function getJson(http:Caller caller, http:Request request) {
        http:Response response = new;
        var payload = request.getJsonPayload();
        if (payload is json) {
            response.setPayload(<@untainted> payload);
        } else {
            error err = payload;
            string? errMsg = err.detail()?.message;
            response.setPayload(errMsg is string ? <@untainted>errMsg : "Error in parsing payload");
        }
        checkpanic caller->respond(response);
    }
}

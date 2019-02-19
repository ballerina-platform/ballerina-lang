import ballerina/http;
import ballerina/mime;
import ballerina/io;

function testContentType(http:Response res, string contentTypeValue) returns string? {
    res.setContentType(contentTypeValue);
    return res.getContentType();
}

function testGetContentLength(http:Response res) returns string {
    return res.getHeader("content-length");
}

function testAddHeader(http:Response res, string key, string value) returns http:Response {
    res.addHeader(key, value);
    return res;
}

function testGetHeader(http:Response res, string key) returns string {
    string contentType = res.getHeader(key);
    return contentType;
}

function testGetHeaders(http:Response res, string key) returns string[] {
    return res.getHeaders(key);
}

function testGetJsonPayload(http:Response res) returns json|error {
    return res.getJsonPayload();
}

function testGetTextPayload (http:Response res) returns (string | error) {
    return res.getTextPayload();
}

function testGetBinaryPayload(http:Response res) returns byte[]|error {
    return res.getBinaryPayload();
}

function testGetXmlPayload(http:Response res) returns xml|error {
    return res.getXmlPayload();
}

function testSetPayloadAndGetText((string|xml|json|byte[]|io:ReadableByteChannel) payload) returns string|error {
    http:Response res = new;
    res.setPayload(payload);
    return res.getTextPayload();
}

function testRemoveHeader(http:Response res, string key) returns http:Response {
    res.setHeader("Expect", "100-continue");
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders(http:Response res) returns http:Response {
    res.removeAllHeaders();
    return res;
}

function testSetHeader(string key, string value) returns http:Response {
    http:Response res = new;
    res.setHeader(key, value);
    return res;
}

function testSetJsonPayload(json value) returns http:Response {
    http:Response res = new;
    res.setJsonPayload(value);
    return res;
}

function testSetStringPayload(string value) returns http:Response {
    http:Response res = new;
    res.setTextPayload(value);
    return res;
}

function testSetXmlPayload(xml value) returns http:Response {
    http:Response res = new;
    res.setXmlPayload(value);
    return res;
}

listener http:MockListener mockEP = new(9090);

@http:ServiceConfig {basePath : "/hello"}
service hello on mockEP {

    @http:ResourceConfig {
        path:"/11"
    }
    resource function echo1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/12/{phase}"
    }
    resource function echo2 (http:Caller caller, http:Request req, string phase) {
        http:Response res = new;
        res.reasonPhrase = phase;
        _ = caller->respond(untaint res);
    }

    @http:ResourceConfig {
        path:"/13"
    }
    resource function echo3 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.statusCode = 203;
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource function addheader (http:Caller caller, http:Request req, string key, string value) {
        http:Response res = new;
        res.addHeader(untaint key, value);
        string result = untaint res.getHeader(untaint key);
        res.setJsonPayload({lang:result});
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/getHeader/{header}/{value}"
    }
    resource function getHeader (http:Caller caller, http:Request req, string header, string value) {
        http:Response res = new;
        res.setHeader(untaint header, value);
        string result = untaint res.getHeader(untaint header);
        res.setJsonPayload({value:result});
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/getJsonPayload/{value}"
    }
    resource function getJsonPayload(http:Caller caller, http:Request req, string value) {
        http:Response res = new;
        json jsonStr = {lang:value};
        res.setJsonPayload(untaint jsonStr);
        var returnResult = res.getJsonPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            res.setJsonPayload(untaint returnResult.lang);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/GetTextPayload/{valueStr}"
    }
    resource function getTextPayload(http:Caller caller, http:Request req, string valueStr) {
        http:Response res = new;
        res.setTextPayload(untaint valueStr);
        var returnResult = res.getTextPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode =500;
        } else {
            res.setTextPayload(untaint returnResult);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/GetXmlPayload"
    }
    resource function getXmlPayload(http:Caller caller, http:Request req) {
        http:Response res = new;
        xml xmlStr = xml `<name>ballerina</name>`;
        res.setXmlPayload(xmlStr);
        var returnResult = res.getXmlPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode =500;
        } else {
            var name = returnResult.getTextValue();
            res.setTextPayload(untaint name);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/RemoveHeader/{key}/{value}"
    }
    resource function removeHeader (http:Caller caller, http:Request req, string key, string value) {
        http:Response res = new;
        res.setHeader(untaint key, value);
        res.removeHeader(untaint key);
        string header = "";
        if (!res.hasHeader(key)) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource function removeAllHeaders (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader("Expect", "100-continue");
        res.setHeader("Range", "bytes=500-999");
        res.removeAllHeaders();
        string header = "";
        if(!res.hasHeader("Range")) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = caller->respond(res);
    }
}

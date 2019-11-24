import ballerina/http;
import ballerina/io;
import ballerina/lang.'string as strings;

function testContentType(http:Request req, string contentTypeValue) returns @tainted string {
    checkpanic req.setContentType(contentTypeValue);
    return req.getContentType();
}

function testGetContentLength(http:Request req) returns @tainted string {
    return req.getHeader("content-length");
}

function testAddHeader(string key, string value) returns http:Request {
    http:Request req = new;
    req.setHeader(key, "1stHeader");
    req.addHeader(key, value);
    return req;
}

function testSetHeader(string key, string value) returns http:Request {
    http:Request req = new;
    req.setHeader(key, "abc");
    req.setHeader(key, value);
    return req;
}

function testSetJsonPayload(json value) returns http:Request {
    http:Request req = new;
    req.setJsonPayload(value);
    return req;
}

function testSetStringPayload(string value) returns http:Request {
    http:Request req = new;
    req.setTextPayload(value);
    return req;
}

function testSetXmlPayload(xml value) returns http:Request {
    http:Request req = new;
    req.setXmlPayload(value);
    return req;
}

function testSetBinaryPayload(byte[] value) returns http:Request {
    http:Request req = new;
    req.setBinaryPayload(value);
    return req;
}

function testSetEntityBody(string filePath, string contentType) returns http:Request {
    http:Request req = new;
    req.setFileAsPayload(filePath, contentType = contentType);
    return req;
}

function testSetPayloadAndGetText((string|xml|json|byte[]|io:ReadableByteChannel) payload) returns
                                    @tainted string|error {
    http:Request req = new;
    req.setPayload(payload);
    return req.getTextPayload();
}

function testGetHeader(http:Request req, string key) returns @tainted string {
    return req.getHeader(key);
}

function testGetHeaders(http:Request req, string key) returns @tainted string[] {
    return req.getHeaders(key);
}

function testGetJsonPayload(http:Request req) returns @tainted json|error {
    return req.getJsonPayload();
}

function testGetMethod(http:Request req) returns string {
    string method = req.method;
    return method;
}

function testGetTextPayload(http:Request req) returns @tainted string|error {
    return req.getTextPayload();
}

function testGetBinaryPayload(http:Request req) returns @tainted byte[]|error {
    return req.getBinaryPayload();
}

function testGetXmlPayload(http:Request req) returns @tainted xml|error {
    return req.getXmlPayload();
}

function testAddCookies(http:Request req) returns http:Request {
    http:Cookie cookie1 = new;
    cookie1.name = "SID1";
    cookie1.value = "31d4d96e407aad42";
    cookie1.domain = "google.com";
    cookie1.path = "/sample";
    http:Cookie cookie2 = new;
    cookie2.name = "SID2";
    cookie2.value = "2638747623468bce72";
    cookie2.domain = "google.com";
    cookie2.path = "/sample/about";
    http:Cookie cookie3 = new;
    cookie3.name = "SID3";
    cookie3.value = "782638747668bce72";
    cookie3.domain = "google.com";
    cookie3.path = "/sample";
    http:Cookie[] cookiesToAdd = [cookie1, cookie2, cookie3];
    req.addCookies(cookiesToAdd);
    return req;
}

function testGetCookies(http:Request req) returns http:Cookie[] {
    http:Cookie cookie1 = new;
    cookie1.name = "SID1";
    cookie1.value = "31d4d96e407aad42";
    cookie1.domain = "google.com";
    cookie1.path = "/sample";
    http:Cookie[] cookiesToAdd = [cookie1];
    req.addCookies(cookiesToAdd);
    http:Cookie[] cookiesInRequest = req.getCookies();
    return cookiesInRequest;
}

listener http:MockListener mockEP = new(9090);

@http:ServiceConfig { basePath: "/hello" }
service hello on mockEP {

    @http:ResourceConfig {
        path: "/addheader/{key}/{value}"
    }
    resource function addheader(http:Caller caller, http:Request inReq, string key, string value) {
        http:Request req = new;
        req.addHeader(<@untainted string> key, value);
        string result = <@untainted string> req.getHeader(<@untainted string> key);
        http:Response res = new;
        res.setJsonPayload({ lang: result });
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/11"
    }
    resource function echo1(http:Caller caller, http:Request req) {
        http:Response res = new;
        string method = req.method;
        res.setTextPayload(<@untainted string> method);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/12"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        http:Response res = new;
        string url = req.rawPath;
        res.setTextPayload(<@untainted string> url);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/13"
    }
    resource function echo3(http:Caller caller, http:Request req) {
        http:Response res = new;
        string url = req.rawPath;
        res.setTextPayload(<@untainted string> url);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/getHeader"
    }
    resource function getHeader(http:Caller caller, http:Request req) {
        http:Response res = new;
        string header = <@untainted string> req.getHeader("content-type");
        res.setJsonPayload({ value: header });
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/getJsonPayload"
    }
    resource function getJsonPayload(http:Caller caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getJsonPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            res.setJsonPayload(<@untainted json> returnResult.lang);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/GetTextPayload"
    }
    resource function getTextPayload(http:Caller caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getTextPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            res.setTextPayload(<@untainted string> returnResult);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/GetXmlPayload"
    }
    resource function getXmlPayload(http:Caller caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getXmlPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            var name = returnResult.getTextValue();
            res.setTextPayload(<@untainted string> name);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/GetBinaryPayload"
    }
    resource function getBinaryPayload(http:Caller caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getBinaryPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            var name = strings:fromBytes(returnResult);
            if (name is string) {
                res.setTextPayload(<@untainted string> name);
            } else {
                res.setTextPayload("Error occurred while byte array to string conversion");
                res.statusCode = 500;
            }
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/GetByteChannel"
    }
    resource function getByteChannel(http:Caller caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getByteChannel();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            res.setByteChannel(returnResult);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/RemoveHeader"
    }
    resource function removeHeader(http:Caller caller, http:Request inReq) {
        http:Request req = new;
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.removeHeader("Content-Type");
        string header = "";
        if (!req.hasHeader("Content-Type")) {
            header = "value is null";
        }
        http:Response res = new;
        res.setJsonPayload({ value: header });
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/RemoveAllHeaders"
    }
    resource function removeAllHeaders(http:Caller caller, http:Request inReq) {
        http:Request req = new;
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setHeader("Expect", "100-continue");
        req.setHeader("Range", "bytes=500-999");
        req.removeAllHeaders();
        string header = "";
        if (!req.hasHeader("Range")) {
            header = "value is null";
        }
        http:Response res = new;
        res.setJsonPayload({ value: header });
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/setHeader/{key}/{value}"
    }
    resource function setHeader(http:Caller caller, http:Request inReq, string key, string value) {
        http:Request req = new;
        req.setHeader(<@untainted string> key, "abc");
        req.setHeader(<@untainted string> key, value);
        string result = <@untainted string> req.getHeader(<@untainted string> key);

        http:Response res = new;
        res.setJsonPayload({ value: result });
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/SetJsonPayload/{value}"
    }
    resource function setJsonPayload(http:Caller caller, http:Request inReq, string value) {
        http:Request req = new;
        json jsonStr = { lang: value };
        req.setJsonPayload(<@untainted json> jsonStr);
        var returnResult = req.getJsonPayload();
        http:Response res = new;
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            res.setJsonPayload(<@untainted json> returnResult);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/SetStringPayload/{value}"
    }
    resource function setStringPayload(http:Caller caller, http:Request inReq, string value) {
        http:Request req = new;
        req.setTextPayload(<@untainted string> value);
        http:Response res = new;
        var returnResult = req.getTextPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            res.setJsonPayload({ lang: <@untainted string> returnResult });
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/SetXmlPayload"
    }
    resource function setXmlPayload(http:Caller caller, http:Request inReq) {
        http:Request req = new;
        xml xmlStr = xml `<name>Ballerina</name>`;
        req.setXmlPayload(xmlStr);
        http:Response res = new;
        var returnResult = req.getXmlPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            var name = <@untainted string> returnResult.getTextValue();
            res.setJsonPayload({ lang: name });
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/SetBinaryPayload"
    }
    resource function setBinaryPayload(http:Caller caller, http:Request inReq) {
        http:Request req = new;
        string text = "Ballerina";
        byte[] payload = text.toBytes();
        req.setBinaryPayload(payload);
        http:Response res = new;
        var returnResult = req.getBinaryPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            var name = strings:fromBytes(returnResult);
            if (name is string) {
                res.setJsonPayload({ lang: <@untainted string> name });
            } else {
                res.setTextPayload("Error occurred while byte array to string conversion");
                res.statusCode = 500;
            }
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/addCookies"
    }
    resource function addCookies(http:Caller caller, http:Request inReq) {
        http:Request req = new;
        http:Cookie cookie1 = new;
        cookie1.name = "SID1";
        cookie1.value = "31d4d96e407aad42";
        cookie1.domain = "google.com";
        cookie1.path = "/sample";
        http:Cookie cookie2 = new;
        cookie2.name = "SID2";
        cookie2.value = "2638747623468bce72";
        cookie2.domain = "google.com";
        cookie2.path = "/sample/about";
        http:Cookie cookie3 = new;
        cookie3.name = "SID3";
        cookie3.value = "782638747668bce72";
        cookie3.domain = "google.com";
        cookie3.path = "/sample";
        http:Cookie[] cookiesToAdd = [cookie1, cookie2, cookie3];
        req.addCookies(cookiesToAdd);
        string result = <@untainted string> req.getHeader("Cookie");
        http:Response res = new;
        res.setJsonPayload({ cookieHeader: result });
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/getCookies"
    }
    resource function getCookies(http:Caller caller, http:Request req) {
        http:Response res = new;
        http:Cookie cookie1 = new;
        cookie1.name = "SID1";
        cookie1.value = "31d4d96e407aad42";
        cookie1.domain = "google.com";
        cookie1.path = "/sample";
        http:Cookie[] cookiesToAdd = [cookie1];
        req.addCookies(cookiesToAdd);
        http:Cookie[] cookiesInRequest = req.getCookies();
        res.setTextPayload(<@untainted string>  cookiesInRequest[0].name );
        checkpanic caller->respond(res);
    }
}

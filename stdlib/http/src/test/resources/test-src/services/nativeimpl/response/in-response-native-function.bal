import ballerina/http;
import ballerina/io;

function testContentType(http:Response res, string contentTypeValue) returns @tainted string? {
    res.setContentType(contentTypeValue);
    return res.getContentType();
}

function testGetContentLength(http:Response res) returns @tainted string {
    return res.getHeader("content-length");
}

function testAddHeader(http:Response res, string key, string value) returns http:Response {
    res.addHeader(key, value);
    return res;
}

function testGetHeader(http:Response res, string key) returns @tainted string {
    string contentType = res.getHeader(key);
    return contentType;
}

function testGetHeaders(http:Response res, string key) returns @tainted string[] {
    return res.getHeaders(key);
}

function testGetJsonPayload(http:Response res) returns @tainted json|error {
    return res.getJsonPayload();
}

function testGetTextPayload (http:Response res) returns @tainted string|error {
    return res.getTextPayload();
}

function testGetBinaryPayload(http:Response res) returns  @tainted byte[]|error {
    return res.getBinaryPayload();
}

function testGetXmlPayload(http:Response res) returns @tainted xml|error {
    return res.getXmlPayload();
}

function testSetPayloadAndGetText((string|xml|json|byte[]|io:ReadableByteChannel) payload) returns
                                    @tainted string|error {
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

function testAddCookie(http:Response res) returns http:Response {
     http:Cookie cookie = new;
     cookie.name = "SID3";
     cookie.value = "31d4d96e407aad42";
     cookie.domain = "google.com";
     cookie.path = "/sample";
     cookie.maxAge = 3600 ;
     cookie.expires = "2017-06-26 05:46:22";
     cookie.httpOnly = true;
     cookie.secure = true;
     res.addCookie(cookie);
     return res;
}

function testRemoveCookiesFromRemoteStore(http:Response res)  returns http:Response {
     http:Cookie cookie = new;
     cookie.name = "SID3";
     cookie.value = "31d4d96e407aad42";
     cookie.expires = "2017-06-26 05:46:22";
     res.removeCookiesFromRemoteStore(cookie);
     return res;
}

function testGetCookies(http:Response res) returns @tainted http:Cookie[] {
     http:Cookie cookie1 = new;
     cookie1.name = "SID002";
     cookie1.value = "239d4dmnmsddd34";
     cookie1.path = "/sample";
     cookie1.domain = ".GOOGLE.com.";
     cookie1.maxAge = 3600 ;
     cookie1.expires = "2017-06-26 05:46:22";
     cookie1.httpOnly = true;
     cookie1.secure = true;
     res.addCookie(cookie1);
     // Gets the added cookies from response.
     http:Cookie[] cookiesInResponse=res.getCookies();
     return cookiesInResponse;
}

listener http:MockListener mockEP = new(9090);

@http:ServiceConfig {basePath : "/hello"}
service hello on mockEP {

    @http:ResourceConfig {
        path:"/11"
    }
    resource function echo1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/12/{phase}"
    }
    resource function echo2 (http:Caller caller, http:Request req, string phase) {
        http:Response res = new;
        res.reasonPhrase = phase;
        checkpanic caller->respond(<@untainted http:Response> res);
    }

    @http:ResourceConfig {
        path:"/13"
    }
    resource function echo3 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.statusCode = 203;
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource function addheader (http:Caller caller, http:Request req, string key, string value) {
        http:Response res = new;
        res.addHeader(<@untainted string> key, value);
        string result = <@untainted string> res.getHeader(<@untainted string> key);
        res.setJsonPayload({lang:result});
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/getHeader/{header}/{value}"
    }
    resource function getHeader (http:Caller caller, http:Request req, string header, string value) {
        http:Response res = new;
        res.setHeader(<@untainted string> header, value);
        string result = <@untainted string> res.getHeader(<@untainted string> header);
        res.setJsonPayload({value:result});
        checkpanic caller->respond(<@untainted> res);
    }

    @http:ResourceConfig {
        path:"/getJsonPayload/{value}"
    }
    resource function getJsonPayload(http:Caller caller, http:Request req, string value) {
        http:Response res = new;
        json jsonStr = {lang:value};
        res.setJsonPayload(<@untainted json> jsonStr);
        var returnResult = res.getJsonPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else {
            res.setJsonPayload(<@untainted json> returnResult.lang);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/GetTextPayload/{valueStr}"
    }
    resource function getTextPayload(http:Caller caller, http:Request req, string valueStr) {
        http:Response res = new;
        res.setTextPayload(<@untainted string> valueStr);
        var returnResult = res.getTextPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode =500;
        } else {
            res.setTextPayload(<@untainted string> returnResult);
        }
        checkpanic caller->respond(res);
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
            res.setTextPayload(<@untainted string> name);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/RemoveHeader/{key}/{value}"
    }
    resource function removeHeader (http:Caller caller, http:Request req, string key, string value) {
        http:Response res = new;
        res.setHeader(<@untainted string> key, value);
        res.removeHeader(<@untainted string> key);
        string header = "";
        if (!res.hasHeader(<@untainted> key)) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        checkpanic caller->respond(<@untainted> res);
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
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/addCookie"
    }
    resource function addCookie (http:Caller caller, http:Request req) {
        http:Response res = new;
        http:Cookie cookie = new;
        cookie.name = "SID3";
        cookie.value = "31d4d96e407aad42";
        cookie.domain = "google.com";
        cookie.path = "/sample";
        cookie.maxAge = 3600 ;
        cookie.expires = "2017-06-26 05:46:22";
        cookie.httpOnly = true;
        cookie.secure = true;
        res.addCookie(cookie);
        string result = <@untainted string> res.getHeader(<@untainted string> "Set-Cookie");
        res.setJsonPayload({SetCookieHeader:result});
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/removeCookieByServer"
    }
    resource function removeCookieByServer (http:Caller caller, http:Request req) {
        http:Response res = new;
        http:Cookie cookie = new;
        cookie.name="SID3";
        cookie.value="31d4d96e407aad42";
        cookie.expires="2017-06-26 05:46:22";
        res.removeCookiesFromRemoteStore(cookie);
        string result = <@untainted string> res.getHeader(<@untainted string> "Set-Cookie");
        res.setJsonPayload({SetCookieHeader:result});
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/getCookies"
    }
    resource function getCookies (http:Caller caller, http:Request req) {
        http:Response res = new;
        http:Cookie cookie1 = new;
        cookie1.name = "SID002";
        cookie1.value = "239d4dmnmsddd34";
        cookie1.path = "/sample";
        cookie1.domain = ".GOOGLE.com.";
        cookie1.maxAge = 3600 ;
        cookie1.expires = "2017-06-26 05:46:22";
        cookie1.httpOnly = true;
        cookie1.secure = true;
        res.addCookie(cookie1);
        //Gets the added cookies from response.
        http:Cookie[] cookiesInResponse=res.getCookies();
        string result = <@untainted string>  cookiesInResponse[0].name ;
        res.setJsonPayload({cookie:result});
        checkpanic caller->respond(res);
    }
}

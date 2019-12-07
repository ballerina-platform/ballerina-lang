import ballerina/http;
import ballerina/mime;

function testGetHeader(http:Response res, string key) returns @tainted string {
    return res.getHeader(key);
}

function testGetJsonPayload(http:Response res) returns @tainted json|error {
    return res.getJsonPayload();
}

function testGetTextPayload(http:Response res) returns @tainted string|error {
    return res.getTextPayload();
}

function testGetBinaryPayload(http:Response res) returns @tainted byte[]|error {
    return res.getBinaryPayload();
}

function testGetXmlPayload(http:Response res) returns @tainted xml|error {
    return res.getXmlPayload();
}

function testGetEntity(http:Response response) returns mime:Entity|error {
    return response.getEntity();
}


function testRemoveHeader(http:Response res, string key) returns (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders(http:Response ress) returns (http:Response) {
    http:Response res = new;
    res.removeAllHeaders();
    return res;
}

function testAddCookieWithInvalidName(http:Response res) returns http:Response {
    http:Cookie cookie = new("    ", "AD4567323");
    cookie.path = "/sample";
    cookie.expires = "2017-06-26 05:46:22";
    res.addCookie(cookie);
    return res;
}

function testAddCookieWithInvalidValue(http:Response res) returns http:Response {
     http:Cookie cookie = new("SID002", "");
     cookie.path = "/sample";
     cookie.expires = "2017-06-26 05:46:22";
     res.addCookie(cookie);
     return res;
}

function testAddCookieWithInvalidPath1(http:Response res) returns http:Response {
     http:Cookie cookie = new("SID002", "AD4567323");
     cookie.path = "sample";
     cookie.expires = "2017-06-26 05:46:22";
     res.addCookie(cookie);
     return res;
}

function testAddCookieWithInvalidPath2(http:Response res) returns http:Response {
     http:Cookie cookie = new("SID002", "AD4567323");
     cookie.path = "/sample?test=123";
     cookie.expires = "2017-06-26 05:46:22";
     res.addCookie(cookie);
     return res;
}

function testAddCookieWithInvalidPath3(http:Response res) returns http:Response {
     http:Cookie cookie = new("SID002", "AD4567323");
     cookie.path = " ";
     cookie.expires = "2017-06-26 05:46:22";
     res.addCookie(cookie);
     return res;
}

function testAddCookieWithInvalidDomain(http:Response res) returns http:Response {
     http:Cookie cookie = new("SID002", "AD4567323");
     cookie.domain = " ";
     cookie.path = "/sample";
     cookie.expires = "2017-06-26 05:46:22";
     res.addCookie(cookie);
     return res;
}

function testAddCookieWithInvalidExpires1(http:Response res) returns http:Response {
     http:Cookie cookie = new("SID002", "AD4567323");
     cookie.path = "/sample";
     cookie.expires = "2017 13 42 05:70:22";
     res.addCookie(cookie);
     return res;
}

function testAddCookieWithInvalidExpires2(http:Response res) returns http:Response {
     http:Cookie cookie = new("SID002", "AD4567323");
     cookie.path = "/sample";
     cookie.expires = " ";
     res.addCookie(cookie);
     return res;
}

function testAddCookieWithInvalidMaxAge(http:Response res) returns http:Response {
     http:Cookie cookie = new("SID002", "AD4567323");
     cookie.path = "/sample";
     cookie.expires = "2017-06-26 05:46:22";
     cookie.maxAge = -3600;
     res.addCookie(cookie);
     return res;
}

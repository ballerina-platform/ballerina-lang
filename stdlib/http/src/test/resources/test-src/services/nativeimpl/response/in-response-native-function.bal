import ballerina/http;
import ballerina/io;

function testContentType(http:Response res, string contentTypeValue) returns @tainted string? {
    checkpanic res.setContentType(contentTypeValue);
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
     http:Cookie cookie = new("SID3", "31d4d96e407aad42");
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
     http:Cookie cookie = new("SID3", "31d4d96e407aad42");
     cookie.expires = "2017-06-26 05:46:22";
     res.removeCookiesFromRemoteStore(cookie);
     return res;
}

function testGetCookies(http:Response res) returns @tainted http:Cookie[] {
     http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
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
            var name = (returnResult/*).toString();
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
        http:Cookie cookie = new("SID3", "31d4d96e407aad42");
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
        http:Cookie cookie = new("SID3", "31d4d96e407aad42");
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
        http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
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

function testTrailingAddHeader(string headerName, string headerValue, string retrieval) returns @tainted string {
    http:Response res = new;
    res.addHeader(headerName, headerValue, position = "trailing");
    return res.getHeader(retrieval, position = "trailing");
}

function testAddingMultipleValuesToSameTrailingHeader() returns @tainted [string[], string ] {
    http:Response res = new;
    res.addHeader("heAder1", "value1", position = "trailing");
    res.addHeader("header1", "value2", position = "trailing");
    return [res.getHeaders("header1", position = "trailing"), res.getHeader("header1", position = "trailing")];
}

function testSetTrailingHeaderAfterAddHeader() returns @tainted [string[], string] {
    http:Response res = new;
    res.addHeader("heAder1", "value1", position = "trailing");
    res.addHeader("header1", "value2", position = "trailing");
    res.addHeader("hEader2", "value3", position = "trailing");
    res.setHeader("HeADEr2", "totally different value", position = "trailing");
    return [res.getHeaders("header1", position = "trailing"), res.getHeader("header2", position = "trailing")];
}

    function testRemoveTrailingHeader() returns @tainted [string[], string] {
    http:Response res = new;
    res.addHeader("heAder1", "value1", position = "trailing");
    res.addHeader("header1", "value2", position = "trailing");
    res.addHeader("header1", "value3", position = "trailing");
    res.addHeader("hEader2", "value3", position = "trailing");
    res.addHeader("headeR2", "value4", position = "trailing");
    res.setHeader("HeADEr2", "totally different value", position = "trailing");
    res.removeHeader("HEADER1", position = "trailing");
    res.removeHeader("NONE_EXISTENCE_HEADER", position = "trailing");
    return [res.getHeaders("header1", position = "trailing"), res.getHeader("header2", position = "trailing")];
}

function testNonExistenceTrailingHeader(string headerName, string headerValue) returns @tainted string {
    http:Response res = new;
    res.addHeader(headerName, headerValue, position = http:TRAILING);
    return res.getHeader("header", position = http:TRAILING);
}

function testGetTrailingHeaderNames() returns @tainted string[] {
    http:Response res = new;
    res.addHeader("heAder1", "value1", position = http:TRAILING);
    res.addHeader("header1", "value2", position = http:TRAILING);
    res.addHeader("header1", "value3", position = http:TRAILING);
    res.addHeader("hEader2", "value3", position = http:TRAILING);
    res.addHeader("headeR2", "value4", position = http:TRAILING);
    res.addHeader("HeADEr2", "totally different value", position = http:TRAILING);
    res.addHeader("HEADER3", "testVal", position = http:TRAILING);
    return res.getHeaderNames(position = http:TRAILING);
}

function testTrailingHasHeader(string headerName, string headerValue) returns boolean{
    http:Response res = new;
    res.setHeader(headerName, headerValue, position = http:TRAILING);
    return res.hasHeader("header1", position = http:TRAILING);
}

function testTrailingHeaderWithNewEntity() returns @tainted [boolean, string[]] {
    http:Response res = new;
    return [res.hasHeader("header2", position = http:TRAILING), res.getHeaderNames(position = http:TRAILING)];
}


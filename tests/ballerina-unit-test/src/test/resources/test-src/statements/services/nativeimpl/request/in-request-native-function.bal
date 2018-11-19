import ballerina/http;
import ballerina/io;
import ballerina/mime;
import ballerina/file;

function testContentType(http:Request req, string contentTypeValue) returns string {
    _ = req.setContentType(contentTypeValue);
    return req.getContentType();
}

function testGetContentLength(http:Request req) returns string {
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

function testSetPayloadAndGetText((string|xml|json|byte[]|io:ReadableByteChannel) payload) returns string|error {
    http:Request req = new;
    req.setPayload(payload);
    return req.getTextPayload();
}

function testGetHeader(http:Request req, string key) returns string {
    return req.getHeader(key);
}

function testGetHeaders(http:Request req, string key) returns string[] {
    return req.getHeaders(key);
}

function testGetJsonPayload(http:Request req) returns json|error {
    return req.getJsonPayload();
}

function testGetMethod(http:Request req) returns string {
    string method = req.method;
    return method;
}

function testGetTextPayload(http:Request req) returns string|error {
    return req.getTextPayload();
}

function testGetBinaryPayload(http:Request req) returns byte[]|error {
    return req.getBinaryPayload();
}

function testGetXmlPayload(http:Request req) returns xml|error {
    return req.getXmlPayload();
}

endpoint http:NonListener mockEP {
    port:9090
};

@http:ServiceConfig { basePath: "/hello" }
service<http:Service> hello bind mockEP {

    @http:ResourceConfig {
        path: "/addheader/{key}/{value}"
    }
    addheader(endpoint caller, http:Request inReq, string key, string value) {
        http:Request req = new;
        req.addHeader(untaint key, value);
        string result = untaint req.getHeader(untaint key);
        http:Response res = new;
        res.setJsonPayload({ lang: result });
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/11"
    }
    echo1(endpoint caller, http:Request req) {
        http:Response res = new;
        string method = req.method;
        res.setTextPayload(untaint method);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/12"
    }
    echo2(endpoint caller, http:Request req) {
        http:Response res = new;
        string url = req.rawPath;
        res.setTextPayload(untaint url);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/13"
    }
    echo3(endpoint caller, http:Request req) {
        http:Response res = new;
        string url = req.rawPath;
        res.setTextPayload(untaint url);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/getHeader"
    }
    getHeader(endpoint caller, http:Request req) {
        http:Response res = new;
        string header = untaint req.getHeader("content-type");
        res.setJsonPayload({ value: header });
        _ = caller->respond(res);
    }

    //Enable this once the getContentLength is added back
    //@http:resourceConfig {
    //    path:"/getContentLength"
    //}
    //GetContentLength (http:ServerConnector conn, http:Request req) {
    //    http:Response res = {};
    //    int length = req.getContentLength();
    //    res.setJsonPayload({value:length});
    //    _ = conn -> respond(res);
    //}

    @http:ResourceConfig {
        path: "/getJsonPayload"
    }
    GetJsonPayload(endpoint caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getJsonPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is json) {
            res.setJsonPayload(untaint returnResult.lang);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/GetTextPayload"
    }
    GetTextPayload(endpoint caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getTextPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is string) {
            res.setTextPayload(untaint returnResult);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/GetXmlPayload"
    }
    GetXmlPayload(endpoint caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getXmlPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is xml) {
            var name = returnResult.getTextValue();
            res.setTextPayload(untaint name);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/GetBinaryPayload"
    }
    GetBinaryPayload(endpoint caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getBinaryPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is byte[]) {
            string name = mime:byteArrayToString(returnResult, "UTF-8");
            res.setTextPayload(untaint name);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/GetByteChannel"
    }
    GetByteChannel(endpoint caller, http:Request req) {
        http:Response res = new;
        var returnResult = req.getByteChannel();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is io:ReadableByteChannel) {
            res.setByteChannel(returnResult);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/RemoveHeader"
    }
    RemoveHeader(endpoint caller, http:Request inReq) {
        http:Request req = new;
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.removeHeader("Content-Type");
        string header = "";
        if (!req.hasHeader("Content-Type")) {
            header = "value is null";
        }
        http:Response res = new;
        res.setJsonPayload({ value: header });
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/RemoveAllHeaders"
    }
    RemoveAllHeaders(endpoint caller, http:Request inReq) {
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
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/setHeader/{key}/{value}"
    }
    setHeader(endpoint caller, http:Request inReq, string key, string value) {
        http:Request req = new;
        req.setHeader(untaint key, "abc");
        req.setHeader(untaint key, value);
        string result = untaint req.getHeader(untaint key);

        http:Response res = new;
        res.setJsonPayload({ value: result });
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/SetJsonPayload/{value}"
    }
    SetJsonPayload(endpoint caller, http:Request inReq, string value) {
        http:Request req = new;
        json jsonStr = { lang: value };
        req.setJsonPayload(untaint jsonStr);
        var returnResult = req.getJsonPayload();
        http:Response res = new;
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is json) {
            res.setJsonPayload(untaint returnResult);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/SetStringPayload/{value}"
    }
    SetStringPayload(endpoint caller, http:Request inReq, string value) {
        http:Request req = new;
        req.setTextPayload(untaint value);
        http:Response res = new;
        var returnResult = req.getTextPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is string) {
            res.setJsonPayload({ lang: untaint returnResult });
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/SetXmlPayload"
    }
    SetXmlPayload(endpoint caller, http:Request inReq) {
        http:Request req = new;
        xml xmlStr = xml `<name>Ballerina</name>`;
        req.setXmlPayload(xmlStr);
        http:Response res = new;
        var returnResult = req.getXmlPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is xml) {
            var name = untaint returnResult.getTextValue();
            res.setJsonPayload({ lang: name });
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path: "/SetBinaryPayload"
    }
    SetBinaryPayload(endpoint caller, http:Request inReq) {
        http:Request req = new;
        string text = "Ballerina";
        byte[] payload = text.toByteArray("UTF-8");
        req.setBinaryPayload(payload);
        http:Response res = new;
        var returnResult = req.getBinaryPayload();
        if (returnResult is error) {
            res.setTextPayload("Error occurred");
            res.statusCode = 500;
        } else if (returnResult is byte[]) {
            string name = untaint mime:byteArrayToString(returnResult, "UTF-8");
            res.setJsonPayload({ lang: name });
        }
        _ = caller->respond(res);
    }
}

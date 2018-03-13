import ballerina.net.http;

import ballerina.mime;
import ballerina.file;

function testAddHeader (string key, string value) (http:Request) {
    http:Request req = {};
    req.setHeader(key, "1stHeader");
    req.addHeader(key, value);
    return req;
}

function testGetContentLength (http:Request req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:Request req, string key) (string) {
    return req.getHeader(key);
}

function testGetHeaders (http:Request req, string key) (string[]) {
    return req.getHeaders(key);
}

function testGetJsonPayload (http:Request req) (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetProperty (http:Request req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:Request req) (string, mime:EntityError) {
    return req.getStringPayload();
}

function testGetBinaryPayload (http:Request req) (blob, mime:EntityError) {
    return req.getBinaryPayload();
}

function testGetXmlPayload (http:Request req) (xml, mime:EntityError) {
    return req.getXmlPayload();
}

function testRemoveHeader (http:Request req, string key) (http:Request) {
    req.removeHeader(key);
    return req;
}

function testRemoveAllHeaders (http:Request req) (http:Request) {
    req.removeAllHeaders();
    return req;
}

function testSetHeader (string key, string value) (http:Request) {
    http:Request req = {};
    req.setHeader(key, "abc");
    req.setHeader(key, value);
    return req;
}

function testSetJsonPayload (json value) (http:Request) {
    http:Request req = {};
    req.setJsonPayload(value);
    return req;
}

function testSetProperty (string name, string value) (http:Request) {
    http:Request req = {};
    req.setProperty(name, value);
    return req;
}

function testSetStringPayload (string value) (http:Request) {
    http:Request req = {};
    req.setStringPayload(value);
    return req;
}

function testSetXmlPayload (xml value) (http:Request) {
    http:Request req = {};
    req.setXmlPayload(value);
    return req;
}

function testSetBinaryPayload (blob value) (http:Request) {
    http:Request req = {};
    req.setBinaryPayload(value);
    return req;
}

function testSetEntityBody (file:File content, string contentType) (http:Request) {
    http:Request req = {};
    req.setFileAsPayload(content, contentType);
    return req;
}

@http:configuration {basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:Connection conn, http:Request inReq, string key, string value) {
        http:Request req = {};
        req.addHeader(key, value);
        string result = req.getHeader(key);

        http:Response res = {};
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getContentLength"
    }
    resource GetContentLength (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        req.setHeader("content-length", "15");
        int length = req.getContentLength();

        http:Response res = {};
        res.setJsonPayload({value:length});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader"
    }
    resource getHeader (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        string header = req.getHeader("Content-Type");

        http:Response res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload"
    }
    resource GetJsonPayload (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        req.setJsonPayload({lang:"ballerina"});
        var value, _ = req.getJsonPayload();
        json lang = value.lang;

        http:Response res = {};
        res.setJsonPayload(lang);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetProperty"
    }
    resource GetProperty (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        req.setProperty("wso2", "Ballerina");
        string property = req.getProperty("wso2");

        http:Response res = {};
        res.setJsonPayload({value:property});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload"
    }
    resource GetStringPayload (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        req.setStringPayload("ballerina");
        var value, _ = req.getStringPayload();

        http:Response res = {};
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        xml xmlStr = xml `<name>ballerina</name>`;
        req.setXmlPayload(xmlStr);
        var value, _ = req.getXmlPayload();
        var name = value.getTextValue();

        http:Response res = {};
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveHeader"
    }
    resource RemoveHeader (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.removeHeader("Content-Type");
        var headerValue = req.getHeader("Content-Type");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        http:Response res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setHeader("Expect", "100-continue");
        req.setHeader("Range", "bytes=500-999");
        req.removeAllHeaders();
        var headerValue = req.getHeader("Range");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        http:Response res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/setHeader/{key}/{value}"
    }
    resource setHeader (http:Connection conn, http:Request inReq, string key, string value) {
        http:Request req = {};
        req.setHeader(key, "abc");
        req.setHeader(key, value);
        string result = req.getHeader(key);

        http:Response res = {};
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetJsonPayload/{value}"
    }
    resource SetJsonPayload (http:Connection conn, http:Request inReq, string value) {
        http:Request req = {};
        json jsonStr = {lang:value};
        req.setJsonPayload(jsonStr);
        var result, _ = req.getJsonPayload();

        http:Response res = {};
        res.setJsonPayload(result);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetProperty/{key}/{value}"
    }
    resource SetProperty (http:Connection conn, http:Request inReq, string key, string value) {
        http:Request req = {};
        req.setProperty(key, value);
        string result = req.getProperty(key);

        http:Response res = {};
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetStringPayload/{value}"
    }
    resource SetStringPayload (http:Connection conn, http:Request inReq, string value) {
        http:Request req = {};
        req.setStringPayload(value);
        var result, _ = req.getStringPayload();

        http:Response res = {};
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetXmlPayload"
    }
    resource SetXmlPayload (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        xml xmlStr = xml `<name>Ballerina</name>`;
        req.setXmlPayload(xmlStr);
        var value, _ = req.getXmlPayload();
        var name = value.getTextValue();

        http:Response res = {};
        res.setJsonPayload({lang:name});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetBinaryPayload"
    }
    resource SetBinaryPayload (http:Connection conn, http:Request inReq) {
        http:Request req = {};
        string text = "Ballerina";
        blob payload = text.toBlob("UTF-8");
        req.setBinaryPayload(payload);
        var value, _ = req.getBinaryPayload();
        string name = value.toString("UTF-8");

        http:Response res = {};
        res.setJsonPayload({lang:name});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetBinaryPayload"
    }
    resource GetBinaryPayload (http:Connection conn, http:Request req) {
        var value, _ = req.getBinaryPayload();
        string name = value.toString("UTF-8");

        http:Response res = {};
        res.setStringPayload(name);
        _ = conn.respond(res);
    }
}
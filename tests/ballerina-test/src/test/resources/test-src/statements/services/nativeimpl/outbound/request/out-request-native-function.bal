import ballerina.net.http;

import ballerina.mime;
import ballerina.file;

function testAddHeader (string key, string value) (http:OutRequest) {
    http:OutRequest req = {};
    req.setHeader(key, "1stHeader");
    req.addHeader(key, value);
    return req;
}

function testGetContentLength (http:InRequest req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:OutRequest req, string key) (string) {
    return req.getHeader(key);
}

function testGetHeaders (http:OutRequest req, string key) (string[]) {
    return req.getHeaders(key);
}

function testGetJsonPayload (http:OutRequest req) (json) {
    json payload = req.getJsonPayload();
    return payload;
}

function testGetProperty (http:InRequest req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:InRequest req) (string) {
    string payload = req.getStringPayload();
    return payload;
}

function testGetBinaryPayload (http:OutRequest req) (blob) {
    blob payload = req.getBinaryPayload();
    return payload;
}

function testGetXmlPayload (http:OutRequest req) (xml) {
    xml payload = req.getXmlPayload();
    return payload;
}

function testRemoveHeader (http:OutRequest req, string key) (http:OutRequest) {
    req.removeHeader(key);
    return req;
}

function testRemoveAllHeaders (http:OutRequest req) (http:OutRequest) {
    req.removeAllHeaders();
    return req;
}

function testSetHeader (string key, string value) (http:OutRequest) {
    http:OutRequest req = {};
    req.setHeader(key, "abc");
    req.setHeader(key, value);
    return req;
}

function testSetJsonPayload (json value) (http:OutRequest) {
    http:OutRequest req = {};
    req.setJsonPayload(value);
    return req;
}

function testSetProperty (string name, string value) (http:OutRequest) {
    http:OutRequest req = {};
    req.setProperty(name, value);
    return req;
}

function testSetStringPayload (string value) (http:OutRequest) {
    http:OutRequest req = {};
    req.setStringPayload(value);
    return req;
}

function testSetXmlPayload (xml value) (http:OutRequest) {
    http:OutRequest req = {};
    req.setXmlPayload(value);
    return req;
}

function testSetBinaryPayload(blob value) (http:OutRequest) {
    http:OutRequest req = {};
    req.setBinaryPayload(value);
    return req;
}

function testSetEntityBody(file:File content, string contentType) (http:OutRequest) {
    http:OutRequest req = {};
    req.setFileAsPayload(content, contentType);
    return req;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:Connection conn, http:InRequest inReq, string key, string value) {
        http:OutRequest req = {};
        req.addHeader(key, value);
        string result = req.getHeader(key);

        http:OutResponse res = {};
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getContentLength"
    }
    resource GetContentLength (http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        req.setHeader("content-length", "15");
        int length = req.getContentLength();

        http:OutResponse res = {};
        res.setJsonPayload({value:length});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader"
    }
    resource getHeader (http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        string header = req.getHeader("Content-Type");

        http:OutResponse res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload"
    }
    resource GetJsonPayload(http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        req.setJsonPayload({lang:"ballerina"});
        json value = req.getJsonPayload();
        json lang = value.lang;

        http:OutResponse res = {};
        res.setJsonPayload(lang);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetProperty"
    }
    resource GetProperty (http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        req.setProperty("wso2", "Ballerina");
        string property = req.getProperty("wso2");

        http:OutResponse res = {};
        res.setJsonPayload({value:property});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload"
    }
    resource GetStringPayload(http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        req.setStringPayload("ballerina");
        string value = req.getStringPayload();

        http:OutResponse res = {};
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        xml xmlStr = xml `<name>ballerina</name>`;
        req.setXmlPayload(xmlStr);
        xml value = req.getXmlPayload();
        string name = value.getTextValue();

        http:OutResponse res = {};
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveHeader"
    }
    resource RemoveHeader (http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.removeHeader("Content-Type");
        var headerValue = req.getHeader("Content-Type");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        http:OutResponse res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setHeader("Expect", "100-continue");
        req.setHeader("Range", "bytes=500-999");
        req.removeAllHeaders();
        var headerValue = req.getHeader("Range");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        http:OutResponse res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/setHeader/{key}/{value}"
    }
    resource setHeader (http:Connection conn, http:InRequest inReq, string key, string value) {
        http:OutRequest req = {};
        req.setHeader(key, "abc");
        req.setHeader(key, value);
        string result = req.getHeader(key);

        http:OutResponse res = {};
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetJsonPayload/{value}"
    }
    resource SetJsonPayload (http:Connection conn, http:InRequest inReq, string value) {
        http:OutRequest req = {};
        json jsonStr = {lang:value};
        req.setJsonPayload(jsonStr);
        json result = req.getJsonPayload();

        http:OutResponse res = {};
        res.setJsonPayload(result);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetProperty/{key}/{value}"
    }
    resource SetProperty (http:Connection conn, http:InRequest inReq, string key, string value) {
        http:OutRequest req = {};
        req.setProperty(key, value);
        string result = req.getProperty(key);

        http:OutResponse res = {};
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetStringPayload/{value}"
    }
    resource SetStringPayload (http:Connection conn, http:InRequest inReq, string value) {
        http:OutRequest req = {};
        req.setStringPayload(value);
        string result = req.getStringPayload();

        http:OutResponse res = {};
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetXmlPayload"
    }
    resource SetXmlPayload (http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        xml xmlStr = xml `<name>Ballerina</name>`;
        req.setXmlPayload(xmlStr);
        xml value = req.getXmlPayload();
        string name = value.getTextValue();

        http:OutResponse res = {};
        res.setJsonPayload({lang:name});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetBinaryPayload"
    }
    resource SetBinaryPayload (http:Connection conn, http:InRequest inReq) {
        http:OutRequest req = {};
        string text = "Ballerina";
        blob payload = text.toBlob("UTF-8");
        req.setBinaryPayload(payload);
        blob value = req.getBinaryPayload();
        string name = value.toString("UTF-8");

        http:OutResponse res = {};
        res.setJsonPayload({lang:name});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetBinaryPayload"
    }
    resource GetBinaryPayload(http:Connection conn, http:InRequest req) {
        blob value = req.getBinaryPayload();
        string name = value.toString("UTF-8");

        http:OutResponse res = {};
        res.setStringPayload(name);
        _ = conn.respond(res);
    }
}
import ballerina.net.http;
import ballerina.file;
import ballerina.mime;

function testAddHeader (http:OutResponse res, string key, string value) (http:OutResponse) {
    res.addHeader(key, value);
    return res;
}

function testGetContentLength (http:OutResponse res) (int) {
    int length = res.getContentLength();
    return length;
}

function testGetHeader (http:OutResponse res, string key) (string) {
    string contentType = res.getHeader(key);
    return contentType;
}

function testGetHeaders (http:OutResponse res, string key) (string[]) {
    return res.getHeaders(key);
}

function testGetJsonPayload (http:OutResponse res) (json, mime:EntityError) {
    return res.getJsonPayload();
}

function testGetProperty (http:OutResponse res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:OutResponse res) (string, mime:EntityError) {
    return res.getStringPayload();
}

function testGetBinaryPayload (http:OutResponse res) (blob, mime:EntityError) {
    return res.getBinaryPayload();
}

function testGetXmlPayload (http:OutResponse res) (xml, mime:EntityError) {
    return res.getXmlPayload();
}

function testRemoveHeader (http:OutResponse res, string key) (http:OutResponse) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:OutResponse res) (http:OutResponse) {
    res.removeAllHeaders();
    return res;
}

function testSetHeader (string key, string value) (http:OutResponse) {
    http:OutResponse res = {};
    res.setHeader(key, value);
    return res;
}

function testSetJsonPayload (json value) (http:OutResponse) {
    http:OutResponse res = {};
    res.setJsonPayload(value);
    return res;
}

function testSetProperty (string name, string value) (http:OutResponse) {
    http:OutResponse res = {};
    res.setProperty(name, value);
    return res;
}

function testSetStringPayload (string value) (http:OutResponse) {
    http:OutResponse res = {};
    res.setStringPayload(value);
    return res;
}

function testSetXmlPayload (xml value) (http:OutResponse) {
    http:OutResponse res = {};
    res.setXmlPayload(value);
    return res;
}

function testSetEntityBody(file:File content, string contentType) (http:OutResponse) {
    http:OutResponse res = {};
    res.setFileAsPayload(content, contentType);
    return res;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:Request req) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/12/{phase}"
    }
    resource echo2 (http:Connection conn, http:Request req, string phase) {
        http:OutResponse res = {};
        res.reasonPhrase = phase;
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Connection conn, http:Request req) {
        http:OutResponse res = {};
        res.statusCode = 203;
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:Connection conn, http:Request req, string key, string value) {
        http:OutResponse res = {};
        res.addHeader(key, value);
        string result = res.getHeader(key);
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader/{header}/{value}"
    }
    resource getHeader (http:Connection conn, http:Request req, string header, string value) {
        http:OutResponse res = {};
        res.setHeader(header, value);
        string result = res.getHeader(header);
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload/{value}"
    }
    resource GetJsonPayload(http:Connection conn, http:Request req, string value) {
        http:OutResponse res = {};
        json jsonStr = {lang:value};
        res.setJsonPayload(jsonStr);
        var result, _ = res.getJsonPayload();
        json lang = result.lang;
        res.setJsonPayload(lang);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetProperty/{key}/{value}"
    }
    resource GetProperty (http:Connection conn, http:Request req, string key, string value) {
        http:OutResponse res = {};
        res.setProperty(key, value);
        string property = res.getProperty(key);
        res.setJsonPayload({value:property});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload/{valueStr}"
    }
    resource GetStringPayload(http:Connection conn, http:Request req, string valueStr) {
        http:OutResponse res = {};
        res.setStringPayload(valueStr);
        var value, _ = res.getStringPayload();
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:Connection conn, http:Request req) {
        http:OutResponse res = {};
        xml xmlStr = xml `<name>ballerina</name>`;
        res.setXmlPayload(xmlStr);
        var value, _ = res.getXmlPayload();
        var name = value.getTextValue();
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveHeader/{key}/{value}"
    }
    resource RemoveHeader (http:Connection conn, http:Request req, string key, string value) {
        http:OutResponse res = {};
        res.setHeader(key, value);
        res.removeHeader(key);
        var headerValue = res.getHeader(key);
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:Connection conn, http:Request req) {
        http:OutResponse res = {};
        res.setHeader("Expect", "100-continue");
        res.setHeader("Range", "bytes=500-999");
        res.removeAllHeaders();
        var headerValue = res.getHeader("Range");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }
}

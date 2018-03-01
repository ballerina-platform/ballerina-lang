import ballerina.net.http;
import ballerina.io;

function testGetContentLength (http:InRequest req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:InRequest req, string key) (string) {
    return req.getHeader(key);
}

function testGetHeaders (http:InRequest req, string key) (string[]) {
    return req.getHeaders(key);
}

function testGetJsonPayload (http:InRequest req) (json) {
    json payload = req.getJsonPayload();
    return payload;
}

function testGetMethod (http:InRequest req) (string) {
    string method = req.method;
    return method;
}

function testGetProperty (http:InRequest req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:InRequest req) (string) {
    string payload = req.getStringPayload();
    return payload;
}

function testGetBinaryPayload (http:InRequest req) (blob) {
    blob payload = req.getBinaryPayload();
    return payload;
}

function testGetXmlPayload (http:InRequest req) (xml) {
    xml payload = req.getXmlPayload();
    return payload;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string method = req.method;
        res.setStringPayload(method);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo2 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string url = req.rawPath;
        res.setStringPayload(url);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string url = req.rawPath;
        res.setStringPayload(url);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getContentLength"
    }
    resource GetContentLength (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        int length = req.getContentLength();
        res.setJsonPayload({value:length});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader"
    }
    resource getHeader (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string header = req.getHeader("content-type");
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload"
    }
    resource GetJsonPayload(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json value = req.getJsonPayload();
        json lang = value.lang;
        res.setJsonPayload(lang);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetProperty"
    }
    resource GetProperty (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string property = req.getProperty("wso2");
        res.setJsonPayload({value:property});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload"
    }
    resource GetStringPayload(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string value = req.getStringPayload();
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        xml value = req.getXmlPayload();
        string name = value.getTextValue();
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetBinaryPayload"
    }
    resource GetBinaryPayload(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        blob value = req.getBinaryPayload();
        string name = value.toString("UTF-8");
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetByteChannel"
    }
    resource GetByteChannel(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        io:ByteChannel byteChannel = req.getByteChannel();
        res.setByteChannel(byteChannel);
        _ = conn.respond(res);
    }
}

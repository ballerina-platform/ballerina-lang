import ballerina/net.http;
import ballerina/io;
import ballerina/mime;

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

function testGetJsonPayload (http:InRequest req) (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetMethod (http:InRequest req) (string) {
    string method = req.method;
    return method;
}

function testGetProperty (http:InRequest req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:InRequest req) (string, mime:EntityError) {
    return req.getStringPayload();
}

function testGetBinaryPayload (http:InRequest req) (blob, mime:EntityError) {
    return req.getBinaryPayload();
}

function testGetXmlPayload (http:InRequest req) (xml, mime:EntityError) {
    return req.getXmlPayload();
}

@http:configuration {basePath:"/hello"}
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
    resource GetJsonPayload (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var value, _ = req.getJsonPayload();
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
    resource GetStringPayload (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var value, _ = req.getStringPayload();
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var value, _ = req.getXmlPayload();
        string name = value.getTextValue();
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetBinaryPayload"
    }
    resource GetBinaryPayload (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var value, _ = req.getBinaryPayload();
        string name = value.toString("UTF-8");
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetByteChannel"
    }
    resource GetByteChannel (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var byteChannel, _ = req.getByteChannel();
        res.setByteChannel(byteChannel);
        _ = conn.respond(res);
    }
}

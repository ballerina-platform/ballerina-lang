import ballerina.net.http;
import ballerina.net.http.mock;
import ballerina.io;
import ballerina.mime;
import ballerina.file;

function testGetContentLength (http:Request req) (int) {
    int length = req.getContentLength();
    return length;
}

function testAddHeader (string key, string value) (http:Request) {
    http:Request req = {};
    req.setHeader(key, "1stHeader");
    req.addHeader(key, value);
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

function testGetHeader (http:Request req, string key) (string) {
    return req.getHeader(key);
}

function testGetHeaders (http:Request req, string key) (string[]) {
    return req.getHeaders(key);
}

function testGetJsonPayload (http:Request req) (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetMethod (http:Request req) (string) {
    string method = req.method;
    return method;
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

endpoint<mock:NonListeningService> mockEP {
    port:9090
}

@http:serviceConfig {endpoints:[mockEP]}
service<http:Service> hello {

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:ServerConnector conn, http:Request inReq, string key, string value) {
        http:Request req = {};
        req.addHeader(key, value);
        string result = req.getHeader(key);

        http:Response res = {};
        res.setJsonPayload({lang:result});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        string method = req.method;
        res.setStringPayload(method);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        string url = req.rawPath;
        res.setStringPayload(url);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        string url = req.rawPath;
        res.setStringPayload(url);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/getContentLength"
    }
    resource GetContentLength (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        int length = req.getContentLength();
        res.setJsonPayload({value:length});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader"
    }
    resource getHeader (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        string header = req.getHeader("content-type");
        res.setJsonPayload({value:header});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload"
    }
    resource GetJsonPayload (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        var value, _ = req.getJsonPayload();
        json lang = value.lang;
        res.setJsonPayload(lang);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/GetProperty"
    }
    resource GetProperty (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        string property = req.getProperty("wso2");
        res.setJsonPayload({value:property});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload"
    }
    resource GetStringPayload (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        var value, _ = req.getStringPayload();
        res.setStringPayload(value);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        var value, _ = req.getXmlPayload();
        string name = value.getTextValue();
        res.setStringPayload(name);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/GetBinaryPayload"
    }
    resource GetBinaryPayload (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        var value, _ = req.getBinaryPayload();
        string name = value.toString("UTF-8");
        res.setStringPayload(name);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/GetByteChannel"
    }
    resource GetByteChannel (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        var byteChannel, _ = req.getByteChannel();
        res.setByteChannel(byteChannel);
        _ = conn -> respond(res);
    }

        @http:resourceConfig {
        path:"/RemoveHeader"
    }
    resource RemoveHeader (http:ServerConnector conn, http:Request inReq) {
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
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:ServerConnector conn, http:Request inReq) {
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
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/setHeader/{key}/{value}"
    }
    resource setHeader (http:ServerConnector conn, http:Request inReq, string key, string value) {
        http:Request req = {};
        req.setHeader(key, "abc");
        req.setHeader(key, value);
        string result = req.getHeader(key);

        http:Response res = {};
        res.setJsonPayload({value:result});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/SetJsonPayload/{value}"
    }
    resource SetJsonPayload (http:ServerConnector conn, http:Request inReq, string value) {
        http:Request req = {};
        json jsonStr = {lang:value};
        req.setJsonPayload(jsonStr);
        var result, _ = req.getJsonPayload();

        http:Response res = {};
        res.setJsonPayload(result);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/SetProperty/{key}/{value}"
    }
    resource SetProperty (http:ServerConnector conn, http:Request inReq, string key, string value) {
        http:Request req = {};
        req.setProperty(key, value);
        string result = req.getProperty(key);

        http:Response res = {};
        res.setJsonPayload({value:result});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/SetStringPayload/{value}"
    }
    resource SetStringPayload (http:ServerConnector conn, http:Request inReq, string value) {
        http:Request req = {};
        req.setStringPayload(value);
        var result, _ = req.getStringPayload();

        http:Response res = {};
        res.setJsonPayload({lang:result});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/SetXmlPayload"
    }
    resource SetXmlPayload (http:ServerConnector conn, http:Request inReq) {
        http:Request req = {};
        xml xmlStr = xml `<name>Ballerina</name>`;
        req.setXmlPayload(xmlStr);
        var value, _ = req.getXmlPayload();
        var name = value.getTextValue();

        http:Response res = {};
        res.setJsonPayload({lang:name});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/SetBinaryPayload"
    }
    resource SetBinaryPayload (http:ServerConnector conn, http:Request inReq) {
        http:Request req = {};
        string text = "Ballerina";
        blob payload = text.toBlob("UTF-8");
        req.setBinaryPayload(payload);
        var value, _ = req.getBinaryPayload();
        string name = value.toString("UTF-8");

        http:Response res = {};
        res.setJsonPayload({lang:name});
        _ = conn -> respond(res);
    }
}

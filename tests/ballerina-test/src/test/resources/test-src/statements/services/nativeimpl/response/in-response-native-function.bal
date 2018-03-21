import ballerina/net.http;
import ballerina/net.http.mock;
import ballerina/mime;

function testGetContentLength (http:Response res) (int) {
    int length = res.getContentLength();
    return length;
}

function testAddHeader (http:Response res, string key, string value) (http:Response) {
    res.addHeader(key, value);
    return res;
}

function testGetHeader (http:Response res, string key) (string) {
    string contentType = res.getHeader(key);
    return contentType;
}

function testGetHeaders (http:Response res, string key) (string[]) {
    return res.getHeaders(key);
}

function testGetJsonPayload (http:Response res) (json, mime:EntityError) {
    return res.getJsonPayload();
}

function testGetProperty (http:Response res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:Response res) (string, mime:EntityError) {
    return res.getStringPayload();
}

function testGetBinaryPayload (http:Response res) (blob, mime:EntityError) {
    return res.getBinaryPayload();
}

function testGetXmlPayload (http:Response res) (xml, mime:EntityError) {
    return res.getXmlPayload();
}

function testRemoveHeader (http:Response res, string key) (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:Response res) (http:Response) {
    res.removeAllHeaders();
    return res;
}

function testSetHeader (string key, string value) (http:Response) {
    http:Response res = {};
    res.setHeader(key, value);
    return res;
}

function testSetJsonPayload (json value) (http:Response) {
    http:Response res = {};
    res.setJsonPayload(value);
    return res;
}

function testSetProperty (string name, string value) (http:Response) {
    http:Response res = {};
    res.setProperty(name, value);
    return res;
}

function testSetStringPayload (string value) (http:Response) {
    http:Response res = {};
    res.setStringPayload(value);
    return res;
}

function testSetXmlPayload (xml value) (http:Response) {
    http:Response res = {};
    res.setXmlPayload(value);
    return res;
}

endpoint<mock:NonListeningService> mockEP {
    port:9090
}

@http:serviceConfig {endpoints:[mockEP]}
service<http:Service> hello {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        _ = conn -> forward(res);
    }

    @http:resourceConfig {
        path:"/12/{phase}"
    }
    resource echo2 (http:ServerConnector conn, http:Request req, string phase) {
        http:Response res = {};
        res.reasonPhrase = phase;
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.statusCode = 203;
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:ServerConnector conn, http:Request req, string key, string value) {
        http:Response res = {};
        res.addHeader(key, value);
        string result = res.getHeader(key);
        res.setJsonPayload({lang:result});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader/{header}/{value}"
    }
    resource getHeader (http:ServerConnector conn, http:Request req, string header, string value) {
        http:Response res = {};
        res.setHeader(header, value);
        string result = res.getHeader(header);
        res.setJsonPayload({value:result});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload/{value}"
    }
    resource GetJsonPayload(http:ServerConnector conn, http:Request req, string value) {
        http:Response res = {};
        json jsonStr = {lang:value};
        res.setJsonPayload(jsonStr);
        var result, _ = res.getJsonPayload();
        json lang = result.lang;
        res.setJsonPayload(lang);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/GetProperty/{key}/{value}"
    }
    resource GetProperty (http:ServerConnector conn, http:Request req, string key, string value) {
        http:Response res = {};
        res.setProperty(key, value);
        string property = res.getProperty(key);
        res.setJsonPayload({value:property});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload/{valueStr}"
    }
    resource GetStringPayload(http:ServerConnector conn, http:Request req, string valueStr) {
        http:Response res = {};
        res.setStringPayload(valueStr);
        var value, _ = res.getStringPayload();
        res.setStringPayload(value);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        xml xmlStr = xml `<name>ballerina</name>`;
        res.setXmlPayload(xmlStr);
        var value, _ = res.getXmlPayload();
        var name = value.getTextValue();
        res.setStringPayload(name);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveHeader/{key}/{value}"
    }
    resource RemoveHeader (http:ServerConnector conn, http:Request req, string key, string value) {
        http:Response res = {};
        res.setHeader(key, value);
        res.removeHeader(key);
        var headerValue = res.getHeader(key);
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setHeader("Expect", "100-continue");
        res.setHeader("Range", "bytes=500-999");
        res.removeAllHeaders();
        var headerValue = res.getHeader("Range");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = conn -> respond(res);
    }
}

import ballerina.net.http;

function testAddHeader (http:Request req, string key, string value) (http:Request) {
    req.addHeader(key, value);
    return req;
}

function testClone (http:Request req) (http:Request) {
    http:Request newReq = req.clone();
    return newReq;
}

function testGetContentLength (http:Request req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:Request req, string key) (string) {
    string contentType = req.getHeader(key);
    return contentType;
}

function testGetJsonPayload (http:Request req) (json) {
    json payload = req.getJsonPayload();
    return payload;
}

function testGetMethod (http:Request req) (string) {
    string method = req.getMethod();
    return method;
}

function testGetProperty (http:Request req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:Request req) (string) {
    string payload = req.getStringPayload();
    return payload;
}

function testGetBinaryPayload (http:Request req) (blob) {
    blob payload = req.getBinaryPayload();
    return payload;
}

function testGetXmlPayload (http:Request req) (xml) {
    xml payload = req.getXmlPayload();
    return payload;
}

function testRemoveHeader (http:Request req, string key) (http:Request) {
    req.removeHeader(key);
    return req;
}

function testRemoveAllHeaders (http:Request req) (http:Request) {
    req.removeAllHeaders();
    return req;
}

function testSetContentLength (http:Request req, int contentLength) (http:Request) {
    req.setContentLength(contentLength);
    return req;
}

function testSetHeader (http:Request req, string key, string value) (http:Request) {
    req.setHeader(key, value);
    return req;
}

function testSetJsonPayload (http:Request req, json value) (http:Request) {
    req.setJsonPayload(value);
    return req;
}

function testSetProperty (http:Request req, string name, string value) (http:Request) {
    req.setProperty(name, value);
    return req;
}

function testSetStringPayload (http:Request req, string value) (http:Request) {
    req.setStringPayload(value);
    return req;
}

function testSetXmlPayload (http:Request req, xml value) (http:Request) {
    req.setXmlPayload(value);
    return req;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Request req, http:Response res) {
        string method = req.getMethod();
        res.setStringPayload(method);
        res.send();
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo2 (http:Request req, http:Response res) {
        string url = req.getRequestURL();
        res.setStringPayload(url);
        res.send();
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Request req, http:Response res) {
        string url = req.getRequestURL();
        res.setStringPayload(url);
        res.send();
    }
}
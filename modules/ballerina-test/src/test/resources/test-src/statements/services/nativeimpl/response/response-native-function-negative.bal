import ballerina.net.http;

function testAddHeader (http:Response res, string key, string value) (http:Response) {
    res.addHeader(key, value);
    return res;
}

function testGetContentLength (http:Response res) (int) {
    int length = res.getContentLength();
    return length;
}

function testGetHeader (http:Response res, string key) (string) {
    string contentType = res.getHeader(key);
    return contentType;
}

function testGetJsonPayload (http:Response res) (json) {
    json payload = res.getJsonPayload();
    return payload;
}

function testGetProperty (http:Response res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:Response res) (string) {
    string payload = res.getStringPayload();
    return payload;
}

function testGetBinaryPayload (http:Response res) (blob) {
    blob payload = res.getBinaryPayload();
    return payload;
}

function testGetXmlPayload (http:Response res) (xml) {
    xml payload = res.getXmlPayload();
    return payload;
}

function testRemoveHeader (http:Response res, string key) (http:Response) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:Response res) (http:Response) {
    res.removeAllHeaders();
    return res;
}

function testSetContentLength (http:Response res, int contentLength) (http:Response) {
    res.setContentLength(contentLength);
    return res;
}

function testSetHeader (http:Response res, string key, string value) (http:Response) {
    res.setHeader(key, value);
    return res;
}

function testSetJsonPayload (http:Response res, json value) (http:Response) {
    res.setJsonPayload(value);
    return res;
}

function testSetProperty (http:Response res, string name, string value) (http:Response) {
    res.setProperty(name, value);
    return res;
}

function testSetStringPayload (http:Response res, string value) (http:Response) {
    res.setStringPayload(value);
    return res;
}

function testSetXmlPayload (http:Response res, xml value) (http:Response) {
    res.setXmlPayload(value);
    return res;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Request req, http:Response res) {
        http:Response resp = {};
        _ = resp.send();
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo2 (http:Request req, http:Response res) {
        http:Response resp = {};
        _ = resp.forward(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Request req, http:Response res) {
        http:Response resp = {};
        _ = res.forward(null);
    }

    @http:resourceConfig {
        path:"/14"
    }
    resource echo4 (http:Request req, http:Response res) {
        http:Response resp = {};
        _ = res.forward({});
    }

    @http:resourceConfig {
        path:"/15"
    }
    resource echo5 (http:Request req, http:Response res) {
        http:Response resp = {};
        res.setStringPayload("wso2");
        resp.setStringPayload("Ballerina");
        _ = res.send();
        _ = res.forward(resp);
    }
}
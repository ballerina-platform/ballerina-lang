import ballerina.net.http;
import ballerina.net.http.response;

function testAddHeader (http:Response res, string key, string value) (http:Response) {
    response:addHeader(res, key, value);
    return res;
}

function testClone (http:Response res) (http:Response) {
    http:Response newres = response:clone(res);
    return newres;
}

function testGetContentLength (http:Response res) (int) {
    int length = response:getContentLength(res);
    return length;
}

function testGetHeader (http:Response res, string key) (string) {
    string contentType = response:getHeader(res, key);
    return contentType;
}

function testGetJsonPayload (http:Response res) (json) {
    json payload = response:getJsonPayload(res);
    return payload;
}


function testGetProperty (http:Response res, string propertyName) (string) {
    string payload = response:getProperty(res, propertyName);
    return payload;
}

function testGetStringPayload (http:Response res) (string) {
    string payload = response:getStringPayload(res);
    return payload;
}

function testGetBinaryPayload (http:Response res) (blob) {
    blob payload = response:getBinaryPayload(res);
    return payload;
}

function testGetXmlPayload (http:Response res) (xml) {
    xml payload = response:getXmlPayload(res);
    return payload;
}

function testRemoveHeader (http:Response res, string key) (http:Response) {
    response:removeHeader(res, key);
    return res;
}

function testRemoveAllHeaders (http:Response res) (http:Response) {
    response:removeAllHeaders(res);
    return res;
}

function testSetContentLength (http:Response res, int contentLength) (http:Response) {
    response:setContentLength(res, contentLength);
    return res;
}

function testSetHeader (http:Response res, string key, string value) (http:Response) {
    response:setHeader(res, key, value);
    return res;
}

function testSetJsonPayload (http:Response res, json value) (http:Response) {
    response:setJsonPayload(res, value);
    return res;
}

function testSetProperty (http:Response res, string name, string value) (http:Response) {
    response:setProperty(res, name, value);
    return res;
}

function testSetStringPayload (http:Response res, string value) (http:Response) {
    response:setStringPayload(res, value);
    return res;
}

function testSetXmlPayload (http:Response res, xml value) (http:Response) {
    response:setXmlPayload(res, value);
    return res;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Request req, http:Response res) {
        response:send(res);
    }

    @http:resourceConfig {
        path:"/12/{phase}"
    }
    resource echo2 (http:Request req, http:Response res, string phase) {
        response:setReasonPhrase(res, phase);
        response:send(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Request req, http:Response res) {
        response:setStatusCode(res, 203);
        response:send(res);
    }
}
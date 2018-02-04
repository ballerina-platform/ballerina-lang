import ballerina.net.http;

function testGetContentLength (http:InResponse res) (int) {
    int length = res.getContentLength();
    return length;
}

function testGetHeader (http:InResponse res, string key) (string) {
    string contentType = res.getHeader(key);
    return contentType;
}

function testGetHeaders (http:InResponse res, string key) (string[]) {
    return res.getHeaders(key);
}

function testGetJsonPayload (http:InResponse res) (json) {
    json payload = res.getJsonPayload();
    return payload;
}

function testGetProperty (http:InResponse res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:InResponse res) (string) {
    string payload = res.getStringPayload();
    return payload;
}

function testGetBinaryPayload (http:InResponse res) (blob) {
    blob payload = res.getBinaryPayload();
    return payload;
}

function testGetXmlPayload (http:InResponse res) (xml) {
    xml payload = res.getXmlPayload();
    return payload;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:InRequest req) {
        http:InResponse res = {};
        _ = conn.forward(res);
    }
}

import ballerina.net.http;
import ballerina.mime;

function testGetContentLength (http:Response res) (int) {
    int length = res.getContentLength();
    return length;
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

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:Request req) {
        http:Response res = {};
        _ = conn.forward(res);
    }
}

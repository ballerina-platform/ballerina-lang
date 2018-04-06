import ballerina/http;
import ballerina/http;
import ballerina/io;
import ballerina/mime;
import ballerina/file;

function testGetContentLength (http:Request req) returns (string) {
    return req.getHeader("content-length");
}

function testAddHeader (string key, string value) returns (http:Request) {
    http:Request req = {};
    req.setHeader(key, "1stHeader");
    req.addHeader(key, value);
    return req;
}

function testSetHeader (string key, string value) returns (http:Request) {
    http:Request req = {};
    req.setHeader(key, "abc");
    req.setHeader(key, value);
    return req;
}

function testSetJsonPayload (json value) returns (http:Request) {
    http:Request req = {};
    req.setJsonPayload(value);
    return req;
}

function testSetStringPayload (string value) returns (http:Request) {
    http:Request req = {};
    req.setStringPayload(value);
    return req;
}

function testSetXmlPayload (xml value) returns (http:Request) {
    http:Request req = {};
    req.setXmlPayload(value);
    return req;
}

function testSetBinaryPayload (blob value) returns (http:Request) {
    http:Request req = {};
    req.setBinaryPayload(value);
    return req;
}

function testSetEntityBody (file:File content, string contentType) returns (http:Request) {
    http:Request req = {};
    req.setFileAsPayload(content, contentType);
    return req;
}

function testGetHeader (http:Request req, string key) returns (string) {
    return req.getHeader(key);
}

function testGetHeaders (http:Request req, string key) returns (string[]) {
    return req.getHeaders(key);
}

function testGetJsonPayload (http:Request req) returns (json | http:PayloadError) {
    return req.getJsonPayload();
}

function testGetMethod (http:Request req) returns (string) {
    string method = req.method;
    return method;
}

function testGetStringPayload (http:Request req) returns (string | http:PayloadError) {
    return req.getStringPayload();
}

function testGetBinaryPayload (http:Request req) returns (blob | http:PayloadError) {
    return req.getBinaryPayload();
}

function testGetXmlPayload (http:Request req) returns (xml | http:PayloadError) {
    return req.getXmlPayload();
}

endpoint http:NonListeningServiceEndpoint mockEP {
    port:9090
};

@http:ServiceConfig {basePath : "/hello"}
service<http:Service> hello bind mockEP {

    @http:ResourceConfig {
        path:"/addheader/{key}/{value}"
    }
    addheader (endpoint conn, http:Request inReq, string key, string value) {
        http:Request req = {};
        req.addHeader(key, value);
        string result = req.getHeader(key);
        http:Response res = {};
        res.setJsonPayload({lang:result});
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/11"
    }
    echo1 (endpoint conn, http:Request req) {
        http:Response res = {};
        string method = req.method;
        res.setStringPayload(method);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/12"
    }
    echo2 (endpoint conn, http:Request req) {
        http:Response res = {};
        string url = req.rawPath;
        res.setStringPayload(url);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/13"
    }
    echo3 (endpoint conn, http:Request req) {
        http:Response res = {};
        string url = req.rawPath;
        res.setStringPayload(url);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/getHeader"
    }
    getHeader (endpoint conn, http:Request req) {
        http:Response res = {};
        string header = req.getHeader("content-type");
        res.setJsonPayload({value:header});
        _ = conn -> respond(res);
    }

    //Enable this once the getContentLength is added back
    //@http:resourceConfig {
    //    path:"/getContentLength"
    //}
    //GetContentLength (http:ServerConnector conn, http:Request req) {
    //    http:Response res = {};
    //    int length = req.getContentLength();
    //    res.setJsonPayload({value:length});
    //    _ = conn -> respond(res);
    //}

    @http:ResourceConfig {
        path:"/getJsonPayload"
    }
    GetJsonPayload (endpoint conn, http:Request req) {
        http:Response res = {};
        var returnResult = req.getJsonPayload();
        match returnResult {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode = 500;
            }
            json payload => {
                res.setJsonPayload(payload.lang);
            }
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/GetStringPayload"
    }
    GetStringPayload (endpoint conn, http:Request req) {
        http:Response res = {};
        match req.getStringPayload() {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode =500;
            }
             string payload =>  res.setStringPayload(payload);
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/GetXmlPayload"
    }
    GetXmlPayload (endpoint conn, http:Request req) {
        http:Response res = {};
        match req.getXmlPayload() {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode =500;
            }
            xml xmlPayload => {
                var name = xmlPayload.getTextValue();
                res.setStringPayload(name);
            }
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/GetBinaryPayload"
    }
    GetBinaryPayload (endpoint conn, http:Request req) {
        http:Response res = {};
        match req.getBinaryPayload() {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode =500;
            }
            blob blobPayload => {
                string name = blobPayload.toString("UTF-8");
                res.setStringPayload(name);
            }
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/GetByteChannel"
    }
    GetByteChannel (endpoint conn, http:Request req) {
        http:Response res = {};
        match req.getByteChannel() {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode =500;
            }
            io:ByteChannel byteChannel => {
                res.setByteChannel(byteChannel);
            }
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/RemoveHeader"
    }
    RemoveHeader (endpoint conn, http:Request inReq) {
        http:Request req = {};
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.removeHeader("Content-Type");
        string header;
        if (!req.hasHeader("Content-Type")) {
            header = "value is null";
        }
        http:Response res = {};
        res.setJsonPayload({value:header});
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/RemoveAllHeaders"
    }
    RemoveAllHeaders (endpoint conn, http:Request inReq) {
        http:Request req = {};
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setHeader("Expect", "100-continue");
        req.setHeader("Range", "bytes=500-999");
        req.removeAllHeaders();
        string header;
        if (!req.hasHeader("Range")) {
            header = "value is null";
        }
        http:Response res = {};
        res.setJsonPayload({value:header});
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/setHeader/{key}/{value}"
    }
    setHeader (endpoint conn, http:Request inReq, string key, string value) {
        http:Request req = {};
        req.setHeader(key, "abc");
        req.setHeader(key, value);
        string result = req.getHeader(key);

        http:Response res = {};
        res.setJsonPayload({value:result});
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/SetJsonPayload/{value}"
    }
    SetJsonPayload (endpoint conn, http:Request inReq, string value) {
        http:Request req = {};
        json jsonStr = {lang:value};
        req.setJsonPayload(jsonStr);
        var returnResult = req.getJsonPayload();
        http:Response res = {};
        match returnResult {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode = 500;
            }
            json payload => {
                res.setJsonPayload(payload);
            }
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/SetStringPayload/{value}"
    }
    SetStringPayload (endpoint conn, http:Request inReq, string value) {
        http:Request req = {};
        req.setStringPayload(value);
        http:Response res = {};
        match req.getStringPayload() {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode =500;
            }
            string payload =>  res.setJsonPayload({lang:payload});
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/SetXmlPayload"
    }
    SetXmlPayload (endpoint conn, http:Request inReq) {
        http:Request req = {};
        xml xmlStr = xml `<name>Ballerina</name>`;
        req.setXmlPayload(xmlStr);
        http:Response res = {};
        match req.getXmlPayload() {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode =500;
            }
            xml xmlPayload => {
                var name = xmlPayload.getTextValue();
                res.setJsonPayload({lang:name});
            }
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/SetBinaryPayload"
    }
    SetBinaryPayload (endpoint conn, http:Request inReq) {
        http:Request req = {};
        string text = "Ballerina";
        blob payload = text.toBlob("UTF-8");
        req.setBinaryPayload(payload);
        http:Response res = {};
        match req.getBinaryPayload() {
            http:PayloadError err => {
                res.setStringPayload("Error occurred");
                res.statusCode =500;
            }
            blob blobPayload => {
            string name = blobPayload.toString("UTF-8");
                res.setJsonPayload({lang:name});
            }
        }
        _ = conn -> respond(res);
    }
}

import ballerina.net.http;

function testGetContentLength (http:Request req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:Request req, string key) (string) {
    var contentType = req.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType.value;
}

function testGetJsonPayload (http:Request req) (json) {
    json payload = req.getJsonPayload();
    return payload;
}

function testGetMethod (http:Request req) (string ) {
    string method = req.getMethod();
    return method;
}

function testGetProperty (http:Request req, string propertyName) (string) {
    string payload = req.getProperty(propertyName);
    return payload;
}

function testGetRequestURL (http:Request req) (string) {
    string url = req.getRequestURL();
    if (url == "") {
        url = "no url";
    }
    return url;
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

function setHeaderToStruct1 (http:Request req) (http:Request) {
    req = {headers : {}};
    req.headers["wso2"] = "ballerina";
    var result = req.getHeader("wso2");
    return req;
}

function setHeaderToStruct2 (http:Request req) (http:Request) {
    req = {headers : {}};
    req.headers["wso2"] = "ballerina";
    req.addHeader("wso2", "middleware");
    return req;
}

function setHeaderToStruct3 (http:Request req) (http:Request) {
    req = {headers : {}};
    req.headers["wso2"] = "ballerina";
    var result = req.getHeaders("wso2");
    return req;
}

function setHeaderToStruct4 (http:Request req) (http:Request) {
    endpoint<http:HttpClient> endPoint {
        create http:HttpClient("http://localhost:9090/echo", {});
    }
    req = {headers : {}};
    req.headers["wso2"] = "ballerina";
    var clientResponse, err = endPoint.get("/", req);
    return req;
}

function setHeaderToStruct5 (http:Request req) (http:Request) {
    endpoint<http:HttpClient> endPoint {
        create http:HttpClient("http://localhost:9090/echo", {});
    }
    req = {headers : {}};
    req.headers["wso2"] = [67,788];
    var clientResponse, err = endPoint.get("/", req);
    return req;
}


@http:configuration{basePath:"/hello"}
service<http> helloServer {

    resource setHeader1 (http:Request req, http:Response res) {
        req.headers["wso2"] = "ballerina";
        string result = req.getHeader("wso2").value;
        res.setJsonPayload({value:result});
        _ = res.send();
    }

    resource setHeader2 (http:Request req, http:Response res, string key, string value) {
        http:HeaderValue[] headers = [{value:value, param:{b:"6", c:7}}, {value:value, param:{a:6}}];
        req.headers["wso2"] = "ballerina";
        string result = req.getHeader(key).value;
        res.setJsonPayload({value:result});
        _ = res.send();
    }
}
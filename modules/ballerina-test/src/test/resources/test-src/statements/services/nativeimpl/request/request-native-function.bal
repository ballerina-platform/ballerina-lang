import ballerina.net.http;

function testAddHeader (http:Request req, string key, string value) (http:Request) {
    req.addHeader(key, value);
    return req;
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
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo2 (http:Request req, http:Response res) {
        string url = req.getRequestURL();
        res.setStringPayload(url);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Request req, http:Response res) {
        string url = req.getRequestURL();
        res.setStringPayload(url);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:Request req, http:Response res, string key, string value) {
        req.addHeader(key, value);
        string result = req.getHeader(key);
        res.setJsonPayload({lang:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getContentLength"
    }
    resource GetContentLength (http:Request req, http:Response res) {
        int length = req.getContentLength();
        res.setJsonPayload({value:length});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getHeader"
    }
    resource getHeader (http:Request req, http:Response res) {
        string header = req.getHeader("Content-Type");
        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getJsonPayload"
    }
    resource GetJsonPayload(http:Request req, http:Response res) {
        json value = req.getJsonPayload();
        json lang = value.lang;
        res.setJsonPayload(lang);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/GetProperty"
    }
    resource GetProperty (http:Request req, http:Response res) {
        string property = req.getProperty("wso2");
        res.setJsonPayload({value:property});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/GetStringPayload"
    }
    resource GetStringPayload(http:Request req, http:Response res) {
        string value = req.getStringPayload();
        res.setStringPayload(value);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:Request req, http:Response res) {
        xml value = req.getXmlPayload();
        string name = value.getTextValue();
        res.setStringPayload(name);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/RemoveHeader"
    }
    resource RemoveHeader (http:Request req, http:Response res) {
        req.removeHeader("Content-Type");
        string header = req.getHeader("Content-Type");
        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:Request req, http:Response res) {
        req.removeAllHeaders();
        string header = req.getHeader("Range");
        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/setContentLength"
    }
    resource SetContentLength (http:Request req, http:Response res) {
        req.setContentLength(100);
        int length = req.getContentLength();
        res.setJsonPayload({value:length});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/setHeader/{key}/{value}"
    }
    resource setHeader (http:Request req, http:Response res, string key, string value) {
        req.setHeader(key, value);
        string result = req.getHeader(key);
        res.setJsonPayload({value:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/SetJsonPayload/{value}"
    }
    resource SetJsonPayload (http:Request req, http:Response res, string value) {
        json jsonStr = {lang:value};
        req.setJsonPayload(jsonStr);
        json result = req.getJsonPayload();
        res.setJsonPayload(result);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/SetProperty/{key}/{value}"
    }
    resource SetProperty (http:Request req, http:Response res, string key, string value) {
        req.setProperty(key, value);
        string result = req.getProperty(key);
        res.setJsonPayload({value:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/SetStringPayload/{value}"
    }
    resource SetStringPayload (http:Request req, http:Response res, string value) {
        req.setStringPayload(value);
        string result = req.getStringPayload();
        res.setJsonPayload({lang:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/SetXmlPayload"
    }
    resource SetXmlPayload (http:Request req, http:Response res) {
        xml xmlStr = xml `<name>Ballerina</name>`;
        req.setXmlPayload(xmlStr);
        xml value = req.getXmlPayload();
        string name = value.getTextValue();
        res.setJsonPayload({lang:name});
        _ = res.send();
    }
}
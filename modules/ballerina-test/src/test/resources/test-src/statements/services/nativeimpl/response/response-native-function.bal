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
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/12/{phase}"
    }
    resource echo2 (http:Request req, http:Response res, string phase) {
        res.setReasonPhrase(phase);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Request req, http:Response res) {
        res.setStatusCode(203);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/14"
    }
    resource echo4 (http:Request req, http:Response res) {
        http:Response resp = {};
        resp.setStringPayload("hello");
        _ = res.forward(resp);
    }

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:Request req, http:Response res, string key, string value) {
        res.addHeader(key, value);
        string result = res.getHeader(key);
        res.setJsonPayload({lang:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getContentLength/{header}/{length}"
    }
    resource GetContentLength (http:Request req, http:Response res, string header, string length) {
        res.setHeader(header, length);
        int result = res.getContentLength();
        res.setJsonPayload({value:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getHeader/{header}/{value}"
    }
    resource getHeader (http:Request req, http:Response res, string header, string value) {
        res.setHeader(header, value);
        string result = res.getHeader(header);
        res.setJsonPayload({value:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getJsonPayload/{value}"
    }
    resource GetJsonPayload(http:Request req, http:Response res, string value) {
        json jsonStr = {lang:value};
        res.setJsonPayload(jsonStr);
        json result = res.getJsonPayload();
        json lang = result.lang;
        res.setJsonPayload(lang);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/GetProperty/{key}/{value}"
    }
    resource GetProperty (http:Request req, http:Response res,string key, string value) {
        res.setProperty(key, value);
        string property = res.getProperty(key);
        res.setJsonPayload({value:property});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/GetStringPayload/{valueStr}"
    }
    resource GetStringPayload(http:Request req, http:Response res, string valueStr) {
        res.setStringPayload(valueStr);
        string value = res.getStringPayload();
        res.setStringPayload(value);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:Request req, http:Response res) {
        xml xmlStr = xml `<name>ballerina</name>`;
        res.setXmlPayload(xmlStr);
        xml value = res.getXmlPayload();
        string name = value.getTextValue();
        res.setStringPayload(name);
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/RemoveHeader/{key}/{value}"
    }
    resource RemoveHeader (http:Request req, http:Response res, string key, string value) {
        res.setHeader(key, value);
        res.removeHeader(key);
        string header = res.getHeader(key);
        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:Request req, http:Response res) {
        res.setHeader("Expect", "100-continue");
        res.setHeader("Range", "bytes=500-999");
        res.removeAllHeaders();
        string header = res.getHeader("Range");
        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/setContentLength"
    }
    resource SetContentLength (http:Request req, http:Response res) {
        res.setContentLength(100);
        int length = res.getContentLength();
        res.setJsonPayload({value:length});
        _ = res.send();
    }
}
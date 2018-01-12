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
    string contentType = req.getHeader(key).value;
    return contentType;
}

function testGetHeaders (http:Request req, string key) (string) {
    var headers = req.getHeaders(key);
    var contentType, _ = (string)headers[1].value;
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

function testSetHeader (http:Request req, string key, string value) (http:Request) {
    req.setHeader(key, value);
    return req;
}

function testSetHeaderStruct (http:Request req, string key, string value) (http:Request) {
    http:HeaderValue[] headers = [{value:value, param:{b:"6", c:7}}, {value:value, param:{a:6}}];
    req.headers[key] = headers;
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

function testSetBinaryPayload(http:Request req, blob value) (http:Request) {
    req.setBinaryPayload(value);
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
        string result = req.getHeader(key).value;
        res.setJsonPayload({lang:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/addReqHeader"
    }
    resource addReqHeader (http:Request req, http:Response res) {
        http:HeaderValue[] headers = [{value:"ballerina", param:{b:"6", c:7}}, {value:"transport", param:{a:6}}];
        req.headers["wso2"] = headers;

        var values  = req.headers["wso2"];
        var valueArr,_ = (http:HeaderValue[]) values;
        string header = valueArr[0].value;

        map param = valueArr[0].param;
        var paramVal,_ = (string)param["b"];

        res.setJsonPayload({headerValue:header, paramValue:paramVal});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/addReqHeaderFunc"
    }
    resource addReqHeaderFunc (http:Request req, http:Response res) {
        http:HeaderValue[] headers = [{value:"ballerina", param:{b:"6", c:7}}, {value:"transport", param:{a:6}}];
        req.headers["wso2"] = headers;
        req.addHeader("wso2", "chamil");

        var values  = req.headers["wso2"];
        var valueArr,_ = (http:HeaderValue[]) values;
        int size =  lengthof valueArr;
        string header = valueArr[2].value;

        res.setJsonPayload({headerValue:header, size:size});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/addReqHeaderWithoutParam"
    }
    resource addReqHeaderNoParam (http:Request req, http:Response res) {
        http:HeaderValue[] headers = [{value:"ballerina", param:{}}, {value:"transport", param:{a:6}}];
        req.headers["wso2"] = headers;

        var values  = req.headers["wso2"];
        var valueArr,_ = (http:HeaderValue[]) values;
        string header = valueArr[0].value;

        map param = valueArr[0].param;
        var paramVal,err = (string)param["b"];

        if (err != null) {
            paramVal = "param is null";
        }
        res.setJsonPayload({headerValue:header, paramValue:paramVal});
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
        string header = req.getHeader("Content-Type").value;
        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getReqHeader"
    }
    resource getReqHeader (http:Request req, http:Response res) {
        var values  = req.headers["Content-Type"];
        var valueArr,_ = (http:HeaderValue[]) values;
        string header = valueArr[0].value;

        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getHeaders"
    }
    resource getHeaders (http:Request req, http:Response res) {
        var headers = req.getHeaders("Content-Type");
        var headerValue, _ = (string)headers[1].value;

        map param = headers[1].param;
        var paramVal,err = (string)param["b"];

        res.setJsonPayload({value:headerValue, paramValue:paramVal});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getReqHeaders"
    }
    resource getReqHeadersStruct (http:Request req, http:Response res) {
        http:HeaderValue[] headers = [{value:"ballerina"}, {value:"transport", param:{a:6}}];
        req.headers["wso2"] = headers;

        var headerValues = req.getHeaders("wso2");
        var headerValue, _ = (string)headerValues[1].value;
        res.setJsonPayload({value:headerValue});
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
        var headerValue = req.getHeader("Content-Type");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:Request req, http:Response res) {
        req.removeAllHeaders();
        var headerValue = req.getHeader("Range");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/setHeader/{key}/{value}"
    }
    resource setHeader (http:Request req, http:Response res, string key, string value) {
        req.setHeader(key, value);
        string result = req.getHeader(key).value;
        res.setJsonPayload({value:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/setHeaderStruct/{key}/{value}"
    }
    resource setHeaderStruct (http:Request req, http:Response res, string key, string value) {
        http:HeaderValue[] headers = [{value:value, param:{b:"6", c:7}}, {value:value, param:{a:6}}];
        req.headers[key] = headers;
        string result = req.getHeader(key).value;
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

    @http:resourceConfig {
        path:"/SetBinaryPayload"
    }
    resource SetBinaryPayload (http:Request req, http:Response res) {
        string text = "Ballerina";
        blob payload = text.toBlob("UTF-8");
        req.setBinaryPayload(payload);
        blob value = req.getBinaryPayload();
        string name = value.toString("UTF-8");
        res.setJsonPayload({lang:name});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/GetBinaryPayload"
    }
    resource GetBinaryPayload(http:Request req, http:Response res) {
        blob value = req.getBinaryPayload();
        string name = value.toString("UTF-8");
        res.setStringPayload(name);
        _ = res.send();
    }
}
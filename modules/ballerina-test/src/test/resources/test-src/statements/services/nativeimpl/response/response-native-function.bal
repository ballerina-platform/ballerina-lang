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
    string contentType = res.getHeader(key).value;
    return contentType;
}

function testGetHeaders (http:Response res, string key) (string) {
    var headers = res.getHeaders(key);
    var contentType, _ = (string)headers[1].value;
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

function testSetHeader (http:Response res, string key, string value) (http:Response) {
    res.setHeader(key, value);
    return res;
}

function testSetHeaderStruct (http:Response res, string key, string value) (http:Response) {
    http:HeaderValue[] headers = [{value:value, param:{b:"6", c:7}}, {value:value, param:{a:6}}];
    res.headers[key] = headers;
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
        string result = res.getHeader(key).value;
        res.setJsonPayload({lang:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/addResHeader"
    }
    resource addResHeader (http:Request req, http:Response res) {
        http:HeaderValue[] headers = [{value:"ballerina", param:{b:"6", c:7}}, {value:"transport", param:{a:6}}];
        res.headers["wso2"] = headers;

        var values  = res.headers["wso2"];
        var valueArr,_ = (http:HeaderValue[]) values;
        string header = valueArr[0].value;

        map param = valueArr[0].param;
        var paramVal,_ = (string)param["b"];

        res.setJsonPayload({headerValue:header, paramValue:paramVal});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/addResHeaderFunc"
    }
    resource addResHeaderFunc (http:Request req, http:Response res) {
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
        path:"/addResHeaderWithoutParam"
    }
    resource addResHeaderNoParam (http:Request req, http:Response res) {
        http:HeaderValue[] headers = [{value:"ballerina", param:{}}, {value:"transport", param:{a:6}}];
        res.headers["wso2"] = headers;

        var values  = res.headers["wso2"];
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
        string result = res.getHeader(header).value;
        res.setJsonPayload({value:result});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getResHeader"
    }
    resource getResHeader (http:Request req, http:Response res) {
        http:HeaderValue[] headers = [{value:"ballerina"}, {value:"transport", param:{a:6}}];
        res.headers["wso2"] = headers;
        var values  = res.headers["wso2"];
        var valueArr,_ = (http:HeaderValue[]) values;
        string header = valueArr[0].value;

        res.setJsonPayload({value:header});
        _ = res.send();
    }

    @http:resourceConfig {
        path:"/getHeaders"
    }
    resource getHeaders (http:Request req, http:Response res) {
        http:HeaderValue[] headers = [{value:"ballerina"}, {value:"transport", param:{a:"6"}}];
        res.headers["wso2"] = headers;

        var headerss = res.getHeaders("wso2");
        var headerValue, _ = (string)headerss[1].value;

        map param = headers[1].param;
        var paramVal,err = (string)param["a"];

        res.setJsonPayload({value:headerValue, paramValue:paramVal});
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
        var headerValue = res.getHeader(key);
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
        res.setHeader("Expect", "100-continue");
        res.setHeader("Range", "bytes=500-999");
        res.removeAllHeaders();
        var headerValue = res.getHeader("Range");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = res.send();
    }
}

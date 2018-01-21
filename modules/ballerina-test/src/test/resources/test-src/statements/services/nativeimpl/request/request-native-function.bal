import ballerina.net.http;

import ballerina.mime;
import ballerina.file;

function testAddHeader (http:OutRequest req, string key, string value) (http:OutRequest) {
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
    mime:HeaderValue[] headers = [{value:value, param:{b:"6", c:7}}, {value:value, param:{a:6}}];
    req.setHeaders(key, headers);
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

function testSetEntityBody(http:Request req, file:File content, string contentType) (http:Request) {
    req.setEntityBody(content, contentType);
    return req;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:Request req) {
        http:Response res = {};
        string method = req.getMethod();
        res.setStringPayload(method);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo2 (http:Connection conn, http:Request req) {
        http:Response res = {};
        string url = req.getRequestURL();
        res.setStringPayload(url);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Connection conn, http:Request req) {
        http:Response res = {};
        string url = req.getRequestURL();
        res.setStringPayload(url);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:Connection conn, http:Request req, string key, string value) {
        http:Response res = {};
        req.addHeader(key, value);
        string result = req.getHeader(key).value;
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addReqHeader"
    }
    resource addReqHeader (http:Connection conn, http:Request req) {
        mime:HeaderValue[] headers = [{value:"ballerina", param:{b:"6", c:7}}, {value:"transport", param:{a:6}}];
        req.setHeaders("wso2", headers);

        var values = req.getHeaders("wso2");
        var valueArr,_ = (mime:HeaderValue[]) values;
        string header = valueArr[0].value;

        map param = valueArr[0].param;
        var paramVal,_ = (string)param["b"];

        http:Response res = {};
        res.setJsonPayload({headerValue:header, paramValue:paramVal});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addReqHeaderFunc"
    }
    resource addReqHeaderFunc (http:Connection conn, http:Request req) {
        mime:HeaderValue[] headers = [{value:"ballerina", param:{b:"6", c:7}}, {value:"transport", param:{a:6}}];
        req.setHeaders("wso2", headers);
        req.addHeader("wso2", "chamil");

        var values = req.getHeaders("wso2");
        var valueArr,_ = (mime:HeaderValue[]) values;
        int size =  lengthof valueArr;
        string header = valueArr[2].value;

        http:Response res = {};
        res.setJsonPayload({headerValue:header, size:size});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addReqHeaderWithoutParam"
    }
    resource addReqHeaderNoParam (http:Connection conn, http:Request req) {
        mime:HeaderValue[] headers = [{value:"ballerina", param:{}}, {value:"transport", param:{a:6}}];
        req.setHeaders("wso2", headers);

        var values = req.getHeaders("wso2");
        var valueArr,_ = (mime:HeaderValue[]) values;
        string header = valueArr[0].value;

        map param = valueArr[0].param;
        var paramVal,err = (string)param["b"];

        if (err != null) {
            paramVal = "param is null";
        }
        http:Response res = {};
        res.setJsonPayload({headerValue:header, paramValue:paramVal});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getContentLength"
    }
    resource GetContentLength (http:Connection conn, http:Request req) {
        http:Response res = {};
        int length = req.getContentLength();
        res.setJsonPayload({value:length});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader"
    }
    resource getHeader (http:Connection conn, http:Request req) {
        http:Response res = {};
        string header = req.getHeader("Content-Type").value;
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getReqHeader"
    }
    resource getReqHeader (http:Connection conn, http:Request req) {
        var values = req.getHeaders("test-header");
        var valueArr,_ = (mime:HeaderValue[]) values;
        string header = valueArr[0].value;

        http:Response res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeaders"
    }
    resource getHeaders (http:Connection conn, http:Request req) {
        var headers = req.getHeaders("test-header");
        var headerValue, _ = (string)headers[1].value;

        map param = headers[1].param;
        var paramVal,err = (string)param["b"];

        http:Response res = {};
        res.setJsonPayload({value:headerValue, paramValue:paramVal});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getReqHeaders"
    }
    resource getReqHeadersStruct (http:Connection conn, http:Request req) {
        mime:HeaderValue[] headers = [{value:"ballerina"}, {value:"transport", param:{a:6}}];
        req.setHeaders("wso2", headers);

        var headerValues = req.getHeaders("wso2");
        var headerValue, _ = (string)headerValues[1].value;

        http:Response res = {};
        res.setJsonPayload({value:headerValue});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload"
    }
    resource GetJsonPayload(http:Connection conn, http:Request req) {
        http:Response res = {};
        json value = req.getJsonPayload();
        json lang = value.lang;
        res.setJsonPayload(lang);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetProperty"
    }
    resource GetProperty (http:Connection conn, http:Request req) {
        http:Response res = {};
        string property = req.getProperty("wso2");
        res.setJsonPayload({value:property});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload"
    }
    resource GetStringPayload(http:Connection conn, http:Request req) {
        http:Response res = {};
        string value = req.getStringPayload();
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:Connection conn, http:Request req) {
        http:Response res = {};
        xml value = req.getXmlPayload();
        string name = value.getTextValue();
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveHeader"
    }
    resource RemoveHeader (http:Connection conn, http:Request req) {
        req.removeHeader("Content-Type");
        var headerValue = req.getHeader("Content-Type");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        http:Response res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:Connection conn, http:Request req) {
        req.removeAllHeaders();
        var headerValue = req.getHeader("Range");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        http:Response res = {};
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/setHeader/{key}/{value}"
    }
    resource setHeader (http:Connection conn, http:Request req, string key, string value) {
        http:Response res = {};
        req.setHeader(key, value);
        string result = req.getHeader(key).value;
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/setHeaderStruct/{key}/{value}"
    }
    resource setHeaderStruct (http:Connection conn, http:Request req, string key, string value) {
        mime:HeaderValue[] headers = [{value:value, param:{b:"6", c:7}}, {value:value, param:{a:6}}];
        req.setHeaders(key, headers);
        string result = req.getHeader(key).value;
        http:Response res = {};
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetJsonPayload/{value}"
    }
    resource SetJsonPayload (http:Connection conn, http:Request req, string value) {
        json jsonStr = {lang:value};
        req.setJsonPayload(jsonStr);
        json result = req.getJsonPayload();
        http:Response res = {};
        res.setJsonPayload(result);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetProperty/{key}/{value}"
    }
    resource SetProperty (http:Connection conn, http:Request req, string key, string value) {
        req.setProperty(key, value);
        string result = req.getProperty(key);
        http:Response res = {};
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetStringPayload/{value}"
    }
    resource SetStringPayload (http:Connection conn, http:Request req, string value) {
        req.setStringPayload(value);
        string result = req.getStringPayload();
        http:Response res = {};
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetXmlPayload"
    }
    resource SetXmlPayload (http:Connection conn, http:Request req) {
        xml xmlStr = xml `<name>Ballerina</name>`;
        req.setXmlPayload(xmlStr);
        xml value = req.getXmlPayload();
        string name = value.getTextValue();

        http:Response res = {};
        res.setJsonPayload({lang:name});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/SetBinaryPayload"
    }
    resource SetBinaryPayload (http:Connection conn, http:Request req) {
        string text = "Ballerina";
        blob payload = text.toBlob("UTF-8");
        req.setBinaryPayload(payload);
        blob value = req.getBinaryPayload();
        string name = value.toString("UTF-8");

        http:Response res = {};
        res.setJsonPayload({lang:name});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetBinaryPayload"
    }
    resource GetBinaryPayload(http:Connection conn, http:Request req) {
        http:Response res = {};
        blob value = req.getBinaryPayload();
        string name = value.toString("UTF-8");
        res.setStringPayload(name);
        _ = conn.respond(res);
    }
}
import ballerina.net.http;

import ballerina.mime;
import ballerina.file;

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
    mime:HeaderValue[] headers = [{value:value, param:{b:"6", c:7}}, {value:value, param:{a:6}}];
    res.setHeaders(key, headers);
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

function testSetEntityBody(http:Response res, file:File content, string contentType) (http:Response) {
    res.setEntityBody(content, contentType);
    return res;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:Request req) {
        http:Response res = {};
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/12/{phase}"
    }
    resource echo2 (http:Connection conn, http:Request req, string phase) {
        http:Response res = {};
        res.setReasonPhrase(phase);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Connection conn, http:Request req) {
        http:Response res = {};
        res.setStatusCode(203);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:Connection conn, http:Request req, string key, string value) {
        http:Response res = {};
        res.addHeader(key, value);
        string result = res.getHeader(key).value;
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addResHeader"
    }
    resource addResHeader (http:Connection conn, http:Request req) {
        http:Response res = {};
        mime:HeaderValue[] headers = [{value:"ballerina", param:{b:"6", c:7}}, {value:"transport", param:{a:6}}];
        res.setHeaders("wso2", headers);

        var values = res.getHeaders("wso2");
        var valueArr,_ = (mime:HeaderValue[]) values;
        string header = valueArr[0].value;

        map param = valueArr[0].param;
        var paramVal,_ = (string)param["b"];

        res.setJsonPayload({headerValue:header, paramValue:paramVal});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addResHeaderFunc"
    }
    resource addResHeaderFunc (http:Connection conn, http:Request req) {
        http:Response res = {};
        mime:HeaderValue[] headers = [{value:"ballerina", param:{b:"6", c:7}}, {value:"transport", param:{a:6}}];
        req.setHeaders("wso2", headers);
        req.addHeader("wso2", "chamil");

        var values = req.getHeaders("wso2");
        var valueArr,_ = (mime:HeaderValue[]) values;
        int size =  lengthof valueArr;
        string header = valueArr[2].value;

        res.setJsonPayload({headerValue:header, size:size});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addResHeaderWithoutParam"
    }
    resource addResHeaderNoParam (http:Connection conn, http:Request req) {
        http:Response res = {};
        mime:HeaderValue[] headers = [{value:"ballerina", param:{}}, {value:"transport", param:{a:6}}];
        res.setHeaders("wso2", headers);

        var values = res.getHeaders("wso2");
        var valueArr,_ = (mime:HeaderValue[]) values;
        string header = valueArr[0].value;

        map param = valueArr[0].param;
        var paramVal,err = (string)param["b"];

        if (err != null) {
            paramVal = "param is null";
        }
        res.setJsonPayload({headerValue:header, paramValue:paramVal});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getContentLength/{header}/{length}"
    }
    resource GetContentLength (http:Connection conn, http:Request req, string header, string length) {
        http:Response res = {};
        res.setHeader(header, length);
        int result = res.getContentLength();
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader/{header}/{value}"
    }
    resource getHeader (http:Connection conn, http:Request req, string header, string value) {
        http:Response res = {};
        res.setHeader(header, value);
        string result = res.getHeader(header).value;
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getResHeader"
    }
    resource getResHeader (http:Connection conn, http:Request req) {
        http:Response res = {};
        mime:HeaderValue[] headers = [{value:"ballerina"}, {value:"transport", param:{a:6}}];
        res.setHeaders("wso2", headers);
        var values  = res.getHeaders("wso2");
        var valueArr,_ = (mime:HeaderValue[]) values;
        string header = valueArr[0].value;

        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeaders"
    }
    resource getHeaders (http:Connection conn, http:Request req) {
        http:Response res = {};
        mime:HeaderValue[] headers = [{value:"ballerina"}, {value:"transport", param:{a:"6"}}];
        res.setHeaders("wso2", headers);

        var responseHeaders = res.getHeaders("wso2");
        var headerValue, _ = (string)responseHeaders[1].value;

        map param = headers[1].param;
        var paramVal,err = (string)param["a"];

        res.setJsonPayload({value:headerValue, paramValue:paramVal});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload/{value}"
    }
    resource GetJsonPayload(http:Connection conn, http:Request req, string value) {
        http:Response res = {};
        json jsonStr = {lang:value};
        res.setJsonPayload(jsonStr);
        json result = res.getJsonPayload();
        json lang = result.lang;
        res.setJsonPayload(lang);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetProperty/{key}/{value}"
    }
    resource GetProperty (http:Connection conn, http:Request req,string key, string value) {
        http:Response res = {};
        res.setProperty(key, value);
        string property = res.getProperty(key);
        res.setJsonPayload({value:property});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload/{valueStr}"
    }
    resource GetStringPayload(http:Connection conn, http:Request req, string valueStr) {
        http:Response res = {};
        res.setStringPayload(valueStr);
        string value = res.getStringPayload();
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:Connection conn, http:Request req) {
        http:Response res = {};
        xml xmlStr = xml `<name>ballerina</name>`;
        res.setXmlPayload(xmlStr);
        xml value = res.getXmlPayload();
        string name = value.getTextValue();
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveHeader/{key}/{value}"
    }
    resource RemoveHeader (http:Connection conn, http:Request req, string key, string value) {
        http:Response res = {};
        res.setHeader(key, value);
        res.removeHeader(key);
        var headerValue = res.getHeader(key);
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/RemoveAllHeaders"
    }
    resource RemoveAllHeaders (http:Connection conn, http:Request req) {
        http:Response res = {};
        res.setHeader("Expect", "100-continue");
        res.setHeader("Range", "bytes=500-999");
        res.removeAllHeaders();
        var headerValue = res.getHeader("Range");
        string header;
        if (headerValue == null) {
            header = "value is null";
        }
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }
}

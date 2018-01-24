import ballerina.net.http;

import ballerina.mime;
import ballerina.file;

function testAddHeader (http:OutResponse res, string key, string value) (http:OutResponse) {
    res.addHeader(key, value);
    return res;
}

function testGetContentLength (http:InResponse res) (int) {
    int length = res.getContentLength();
    return length;
}

function testGetHeader (http:InResponse res, string key) (string) {
    string contentType = res.getHeader(key).value;
    return contentType;
}

function testGetHeaders (http:InResponse res, string key) (string) {
    var headers = res.getHeaders(key);
    var contentType, _ = (string)headers[1].value;
    return contentType;
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

function testRemoveHeader (http:OutResponse res, string key) (http:OutResponse) {
    res.removeHeader(key);
    return res;
}

function testRemoveAllHeaders (http:OutResponse res) (http:OutResponse) {
    res.removeAllHeaders();
    return res;
}

function testSetHeader (http:OutResponse res, string key, string value) (http:OutResponse) {
    res.setHeader(key, value);
    return res;
}

function testSetHeaderStruct (http:OutResponse res, string key, string value) (http:OutResponse) {
    mime:HeaderValue[] headers = [{value:value, param:{b:"6", c:7}}, {value:value, param:{a:6}}];
    res.setHeaders(key, headers);
    return res;
}

function testSetJsonPayload (http:OutResponse res, json value) (http:OutResponse) {
    res.setJsonPayload(value);
    return res;
}

function testSetProperty (http:OutResponse res, string name, string value) (http:OutResponse) {
    res.setProperty(name, value);
    return res;
}

function testSetStringPayload (http:OutResponse res, string value) (http:OutResponse) {
    res.setStringPayload(value);
    return res;
}

function testSetXmlPayload (http:OutResponse res, xml value) (http:OutResponse) {
    res.setXmlPayload(value);
    return res;
}

function testSetEntityBody(http:OutResponse res, file:File content, string contentType) (http:OutResponse) {
    res.setEntityBody(content, contentType);
    return res;
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond({});
    }

    @http:resourceConfig {
        path:"/12/{phase}"
    }
    resource echo2 (http:Connection conn, http:InRequest req, string phase) {
        http:OutResponse res = {};
        res.reasonPhrase = phase;
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.statusCode = 203;
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addheader/{key}/{value}"
    }
    resource addheader (http:Connection conn, http:InRequest req, string key, string value) {
        http:OutResponse res = {};
        res.addHeader(key, value);
        string result = res.getHeader(key).value;
        res.setJsonPayload({lang:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addResHeader"
    }
    resource addResHeader (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
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
    resource addResHeaderFunc (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        mime:HeaderValue[] headers = [{value:"ballerina", param:{b:"6", c:7}}, {value:"transport", param:{a:6}}];
        res.setHeaders("wso2", headers);
        res.addHeader("wso2", "chamil");

        var values = res.getHeaders("wso2");
        var valueArr,_ = (mime:HeaderValue[]) values;
        int size =  lengthof valueArr;
        string header = valueArr[2].value;

        res.setJsonPayload({headerValue:header, size:size});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/addResHeaderWithoutParam"
    }
    resource addResHeaderNoParam (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
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
        path:"/getHeader/{header}/{value}"
    }
    resource getHeader (http:Connection conn, http:InRequest req, string header, string value) {
        http:OutResponse res = {};
        res.setHeader(header, value);
        string result = res.getHeader(header).value;
        res.setJsonPayload({value:result});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getResHeader"
    }
    resource getResHeader (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
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
    resource getHeaders (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
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
    resource GetJsonPayload(http:Connection conn, http:InRequest req, string value) {
        http:OutResponse res = {};
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
    resource GetProperty (http:Connection conn, http:InRequest req,string key, string value) {
        http:OutResponse res = {};
        res.setProperty(key, value);
        string property = res.getProperty(key);
        res.setJsonPayload({value:property});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload/{valueStr}"
    }
    resource GetStringPayload(http:Connection conn, http:InRequest req, string valueStr) {
        http:OutResponse res = {};
        res.setStringPayload(valueStr);
        string value = res.getStringPayload();
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
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
    resource RemoveHeader (http:Connection conn, http:InRequest req, string key, string value) {
        http:OutResponse res = {};
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
    resource RemoveAllHeaders (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
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

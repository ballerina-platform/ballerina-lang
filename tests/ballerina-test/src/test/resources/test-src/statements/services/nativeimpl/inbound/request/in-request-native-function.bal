// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina.net.http;
import ballerina.mime;

function testGetContentLength (http:InRequest req) (int) {
    int length = req.getContentLength();
    return length;
}

function testGetHeader (http:InRequest req, string key) (string) {
    return req.getHeader(key);
}

function testGetHeaders (http:InRequest req, string key) (string[]) {
    return req.getHeaders(key);
}

function testGetJsonPayload (http:InRequest req) (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetMethod (http:InRequest req) (string) {
    string method = req.method;
    return method;
}

function testGetStringPayload (http:InRequest req) (string, mime:EntityError) {
    return req.getStringPayload();
}

function testGetBinaryPayload (http:InRequest req) (blob, mime:EntityError) {
    return req.getBinaryPayload();
}

function testGetXmlPayload (http:InRequest req) (xml, mime:EntityError) {
    return req.getXmlPayload();
}

@http:configuration {basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/getProtocol"
    }
    resource GetProtocol (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string protocol = req.protocol;
        res.setJsonPayload({protocol:protocol});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string method = req.method;
        res.setStringPayload(method);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo2 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string url = req.rawPath;
        res.setStringPayload(url);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/13"
    }
    resource echo3 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string url = req.rawPath;
        res.setStringPayload(url);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getContentLength"
    }
    resource GetContentLength (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        int length = req.getContentLength();
        res.setJsonPayload({value:length});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getHeader"
    }
    resource getHeader (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string header = req.getHeader("content-type");
        res.setJsonPayload({value:header});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/getJsonPayload"
    }
    resource GetJsonPayload (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var value, _ = req.getJsonPayload();
        json lang = value.lang;
        res.setJsonPayload(lang);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetStringPayload"
    }
    resource GetStringPayload (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var value, _ = req.getStringPayload();
        res.setStringPayload(value);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetXmlPayload"
    }
    resource GetXmlPayload (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var value, _ = req.getXmlPayload();
        string name = value.getTextValue();
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetBinaryPayload"
    }
    resource GetBinaryPayload (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var value, _ = req.getBinaryPayload();
        string name = value.toString("UTF-8");
        res.setStringPayload(name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/GetByteChannel"
    }
    resource GetByteChannel (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var byteChannel, _ = req.getByteChannel();
        res.setByteChannel(byteChannel);
        _ = conn.respond(res);
    }
}

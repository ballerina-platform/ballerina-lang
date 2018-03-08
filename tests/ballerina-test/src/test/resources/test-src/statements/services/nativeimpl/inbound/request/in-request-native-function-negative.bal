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
    var contentType = req.getHeader(key);
    if (contentType == null) {
        return null;
    }
    return contentType;
}

function testGetJsonPayload (http:InRequest req) (json, mime:EntityError) {
    return req.getJsonPayload();
}

function testGetMethod (http:InRequest req) (string ) {
    string method = req.method;
    return method;
}

function testGetRequestURL (http:InRequest req) (string) {
    string url = req.rawPath;
    if (url == "") {
        url = "no url";
    }
    return url;
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

function testSetHeader (http:OutRequest req, string key, string value) (http:OutRequest) {
    req.setHeader(key, value);
    return req;
}

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

package ballerina.net.http;

import ballerina.mime;

const string HTTP_GET = "GET";
const string HTTP_POST = "POST";
const string HTTP_HEAD = "HEAD";
const string HTTP_PUT = "PUT";
const string HTTP_DELETE = "DELETE";
const string HTTP_PATCH = "PATCH";
const string HTTP_OPTIONS = "OPTIONS";

function getFirstHeaderFromEntity (mime:Entity entity, string headerName) (string) {
    var headerValue, _ = (string[]) entity.headers[headerName];
    return headerValue == null ? null : headerValue[0];
}

function getHeadersFromEntity (mime:Entity entity, string headerName) (string[]) {
    var headerValue, _ = (string[]) entity.headers[headerName];
    return headerValue;
}

function getContentLengthIntValue (string strContentLength) (int) {
    var contentLength, conversionErr = <int>strContentLength;
    if (conversionErr != null) {
        contentLength = -1;
        throw conversionErr;
    }
    return contentLength;
}

function addHeaderToEntity (mime:Entity entity, string headerName, string headerValue){
    var existingValues = entity.headers[headerName];
    if (existingValues == null) {
        setHeaderToEntity(entity, headerName, headerValue);
    } else {
        var valueArray, _ = (string[]) existingValues;
        valueArray[lengthof valueArray] = headerValue;
        entity.headers[headerName] = valueArray;
    }
}

function setHeaderToEntity (mime:Entity entity, string headerName, string headerValue) {
    string[] valueArray = [headerValue];
    entity.headers[headerName] = valueArray;
}

// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import ballerina/lang.'decimal as 'xml;
import ballerina/lang.'int as 'error;
import ballerina/lang.'float as 'object;

function testConcat() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return 'xml:concat(x, "Hello", "hello from String");
}

class CustomListener {
    *'object:Listener;

    public function attach(service object {} s, string[]? name) returns error? {
        // do nothing
    }

    public function detach(service object {} s) returns error? {
        // do nothing
    }

    public function 'start() returns error? {
        // do nothing
    }

    public function gracefulStop() returns error? {
        // do nothing
    }

    public function immediateStop() returns error? {
        // do nothing
    }
}

function testErrorStackTrace('error:CallStackElement elem) returns string {
    return elem.callableName + ":" + elem.fileName;
}

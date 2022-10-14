// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

client "https://postman-echo.com/get?name=simpleclienttest.yaml" as foo;

function testUnusedClientDeclStmtPrefix(boolean b) {
    client "https://postman-echo.com/get?name=simpleclienttest2.yaml" as bar;

    if b {
        client "https://postman-echo.com/get?name=simpleclienttest3.yaml" as baz;
    }
}

client "https://postman-echo.com/get?name=simpleclienttest4.yaml" as p1;

// should result in an error even though p1 is the same module and is used
client "https://postman-echo.com/get?name=simpleclienttest4.yaml" as p2;

public function useModuleClientDeclPrefix() {
    p1:Config _ = {url: ""};
}

public function testUnusedModuleClientDeclPrefixWithSameGeneratedModule() {
    // should result in an error even though the module-level p1 is used and is the same module
    client "https://postman-echo.com/get?name=simpleclienttest4.yaml" as p1;

    client "https://postman-echo.com/get?name=simpleclienttest4.yaml" as p3;
}

client "https://postman-echo.com/get?name=simpleclienttest5.yaml" as p4;

client "https://postman-echo.com/get?name=simpleclienttest5.yaml" as p5;

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

client "http://www.example.com/apis/one.yaml" as foo;

function testModuleClientDecl() {
    foo:ClientConfiguration config = {'limit: 5};
    foo:client cl = new (config);
    int 'limit = cl->getLimit();
    assertEquals(5, 'limit);
    cl->setLimit(10);'limit = cl->getLimit();
    assertEquals(10, 'limit);
}

function testClientDeclStmt() {
    client "http://www.example.com/apis/one.yaml" as bar;
    bar:ClientConfiguration config = {'limit: 5};
    bar:client cl = new (config);
    int 'limit = cl->getLimit();
    assertEquals(5, 'limit);
    cl->setLimit(10);'limit = cl->getLimit();
    assertEquals(10, 'limit);

    client "http://www.example.com/apis/two.yaml" as baz;
    baz:Config config2 = {url: "http://www.example.com"};
    baz:client cl2 = new (config2);
    string url = cl2->getUrl();
    assertEquals("http://www.example.com", url);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}

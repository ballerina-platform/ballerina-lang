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

function testModuleClientDecl() {
    foo:ClientConfiguration config = {'limit: 5};
    foo:client cl = new (config);
    int 'limit = cl->getLimit();
    assertEquals(5, 'limit);
    cl->setLimit(10);'limit = cl->getLimit();
    assertEquals(10, 'limit);
}

function testClientDeclStmt() {
    client "https://postman-echo.com/get?name=simpleclienttest.yaml" as bar;
    bar:ClientConfiguration config = {'limit: 5};
    bar:client cl = new (config);
    int 'limit = cl->getLimit();
    assertEquals(5, 'limit);
    cl->setLimit(10);'limit = cl->getLimit();
    assertEquals(10, 'limit);

    client "https://postman-echo.com/get?name=simpleclienttest2.yaml" as baz;
    baz:Config config2 = {url: "http://www.example.com"};
    baz:client cl2 = new (config2);
    string url = cl2->getUrl();
    assertEquals("http://www.example.com", url);
}

public const annotation record { int i; }[] ClientAnnot on source client;

@ClientAnnot {
    i: 123
}
client "https://postman-echo.com/get?name=simpleclienttest.yaml" as bar;

bar:ClientConfiguration clientConfig = {'limit: 15};

function testClientDeclAnnotSymbols() {
    @ClientAnnot {
        i: 12
    }
    @ClientAnnot {
        i: 13
    }
    client "https://postman-echo.com/get?name=simpleclienttest2.yaml" as qux;
    qux:Config _ = {url: "http://www.example.com/one"};

    client "https://postman-echo.com/get?name=simpleclienttest3.yaml" as quux;
    quux:Config _ = {url: "http://www.example.com/two"};
}

function testClientDeclScoping1() {
    client "https://postman-echo.com/get?name=simpleclienttest2.yaml" as foo; // OK, shadows the module-level prefix
    foo:Config config2 = {url: "http://www.examples.com"};
    foo:client cl2 = new (config2);
    string url = cl2->getUrl();
    assertEquals("http://www.examples.com", url);
}

function testClientDeclScoping2(boolean b) { // no assertion, validates absence of errors
    if b {
        client "https://postman-echo.com/get?name=simpleclienttest.yaml" as bar; // OK, different scope
        bar:ClientConfiguration _ = {'limit: 5};
    } else {
        client "https://postman-echo.com/get?name=simpleclienttest2.yaml" as bar; // OK, different scope
        bar:Config _ = {url: "http://www.examples.com"};
    }

    if !b {
        client "https://postman-echo.com/get?name=simpleclienttest2.yaml" as bar; // OK, different scope
        bar:Config _ = {url: "http://www.examples.com"};
        return;
    }
    client "https://postman-echo.com/get?name=simpleclienttest.yaml" as bar; // OK, different scope
    bar:ClientConfiguration _ = {'limit: 5};
}

function testClientDeclModuleWithClientObjectType() {
    client "https://postman-echo.com/get?name=simpleclienttest-clientobjecttype.yaml" as foo2; // OK, shadows the module-level prefix
    foo2:client cl = foo2:fn();
    int index = cl->getIndex();
    assertEquals(10, index);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}

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

client "http://example.com/apis/one.yaml" as foo;

function testModuleClientDecl() {
    foo:ClientConfiguration config = {'limit: 5};
    foo:client cl = new (config);
    int 'limit = cl->getLimit();
    assertEquals(5, 'limit);
    cl->setLimit(10);'limit = cl->getLimit();
    assertEquals(10, 'limit);
}

function testClientDeclStmt() {
    client "http://example.com/apis/one.yaml" as bar;
    bar:ClientConfiguration config = {'limit: 5};
    bar:client cl = new (config);
    int 'limit = cl->getLimit();
    assertEquals(5, 'limit);
    cl->setLimit(10);'limit = cl->getLimit();
    assertEquals(10, 'limit);

    client "http://example.com/apis/two.yaml" as baz;
    baz:Config config2 = {url: "http://www.example.com"};
    baz:client cl2 = new (config2);
    string url = cl2->getUrl();
    assertEquals("http://www.example.com", url);
}

public const annotation record { int i; }[] ClientAnnot on source client;

@ClientAnnot {
    i: 123
}
client "http://example.com/apis/one.yaml" as bar;

bar:ClientConfiguration clientConfig = {'limit: 15};

function testClientDeclAnnotSymbols() {
    @ClientAnnot {
        i: 12
    }
    @ClientAnnot {
        i: 13
    }
    client "http://example.com/apis/two.yaml" as qux;
    qux:Config _ = {url: "http://www.example.com/one"};

    client "http://example.com/apis/three.yaml" as quux;
    quux:Config _ = {url: "http://www.example.com/two"};
}

function testClientDeclScoping1() {
    client "http://example.com/apis/two.yaml" as foo; // OK, shadows the module-level prefix
    foo:Config config2 = {url: "http://www.examples.com"};
    foo:client cl2 = new (config2);
    string url = cl2->getUrl();
    assertEquals("http://www.examples.com", url);
}

function testClientDeclScoping2(boolean b) { // no assertion, validates absence of errors
    if b {
        client "http://example.com/apis/one.yaml" as bar; // OK, different scope
        bar:ClientConfiguration _ = {'limit: 5};
    } else {
        client "http://example.com/apis/two.yaml" as bar; // OK, different scope
        bar:Config _ = {url: "http://www.examples.com"};
    }

    if !b {
        client "http://example.com/apis/two.yaml" as bar; // OK, different scope
        bar:Config _ = {url: "http://www.examples.com"};
        return;
    }
    client "http://example.com/apis/one.yaml" as bar; // OK, different scope
    bar:ClientConfiguration _ = {'limit: 5};
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}

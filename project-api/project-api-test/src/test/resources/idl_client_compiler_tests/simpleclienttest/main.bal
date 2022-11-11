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
    foo:Client cl = new (config);
    int 'limit = cl->getLimit();
    assertEquals(5, 'limit);
    cl->setLimit(10);'limit = cl->getLimit();
    assertEquals(10, 'limit);
}

public const annotation record { int i; }[] ClientAnnot on source client;

@ClientAnnot {
    i: 123
}
client "https://postman-echo.com/get?name=simpleclienttest.yaml" as bar;

bar:ClientConfiguration clientConfig = {'limit: 15};

client "https://postman-echo.com/get?name=simpleclienttest-clientobjecttype.yaml" as foo3;

function testClientDeclModuleWithClientObjectType() {
    foo3:Client cl = foo3:fn();
    int index = cl->getIndex();
    assertEquals(10, index);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}

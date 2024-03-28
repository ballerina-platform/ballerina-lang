// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import ballerina/test;

@test:Config {}
public function testGetMethod() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello/")
    .onMethod("get").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello;
    test:assertEquals(stringResult, "Hello, Luhee");
    test:prepare(clientEndpoint).whenResource("greeting/hello/").thenReturn("Hello, Lu");
    stringResult = clientEndpoint->/greeting/hello;
    test:assertEquals(stringResult, "Hello, Lu");
}

@test:Config {}
public function testGetWithPathParam() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello/:name").withPathParameters({name: "Luhee"})
    .onMethod("get").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello/["Luhee"];
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testGetWPathParamWSingleArg() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name")
    .withPathParameters({name: "Luhee"}).withArguments("Luhee")
    .onMethod("get").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello2/["Luhee"].get(a = "Luhee");
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testPostMethod() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello/")
    .onMethod("post").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello.post();
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testPostWithPathParam() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello/:name").withPathParameters({name: "Luhee"})
    .onMethod("post").thenReturn("Hello, Luhee");
    test:prepare(clientEndpoint).whenResource("greeting/hello/:name").withPathParameters({name: "bar"})
    .onMethod("post").thenReturn("Hello, bar");
    string stringResult = clientEndpoint->/greeting/hello/["bar"].post();
    test:assertEquals(stringResult, "Hello, bar");
    stringResult = clientEndpoint->/greeting/hello/["Luhee"].post();
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testPostMethodWArgs() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello1/").withArguments("Luhee")
    .onMethod("post").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello1.post(a = "Luhee");
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testPostWPathParamsWSingleArg() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name")
    .withPathParameters({name: "Luhee"}).withArguments("bar")
    .onMethod("post").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello2/["Luhee"].post(a = "bar");
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testPostWAnyPathMatchWSingleArg_1() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello10/:name")
    .withArguments("bar")
    .onMethod("post").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello10/["Luhee"].post(a = "bar");
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testPostWMultiPathParamsWMultiArg() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name/:town")
    .withPathParameters({town: "Jaffna", name: "Luhee"})
    .withArguments("bar", 2).onMethod("post").thenReturn("Hello, Luhee");
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name/:town")
    .withPathParameters({town: "Mallakam", name: "Luhee"})
    .withArguments("foo", 3).onMethod("post").thenReturn("Hello, Luhe");
    string stringResult = clientEndpoint->/greeting/hello2/["Luhee"]/["Jaffna"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "Hello, Luhee");
    stringResult = clientEndpoint->/greeting/hello2/["Luhee"]/["Mallakam"].post(a = "foo", b = 3);
    test:assertEquals(stringResult, "Hello, Luhe");
}

@test:Config {}
public function testPostWMultiPathParamsWMultiArg_1() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:integ/:decim/:str")
    .withPathParameters({integ: 1, decim: <decimal>2, str: "mal"})
    .withArguments("bar", 2).onMethod("post").thenReturn("Hello, Lu");
    string stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "Hello, Lu");
}

@test:Config {}
public function testPostThenReturnSeq() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:integ/:decim/:str").onMethod("post")
    .thenReturnSequence("alp", "gulf", "mulf", "lulf");
    string stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "alp");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "gulf");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "mulf");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "lulf");
}

@test:Config {}
public function testDoNoting1() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello3").onMethod("get").doNothing();
    string? val = clientEndpoint->/greeting/hello3;
    test:assertEquals(val, ());
}

@test:Config {}
public function testDoNoting2() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello4").onMethod("get").doNothing();
    error? val = clientEndpoint->/greeting/hello4;
    test:assertEquals(val, ());
}

@test:Config {}
public function testPathParamsWThenReturnSeq() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:integ/:decim/:str")
    .withPathParameters({integ: 1, decim: <decimal>2, str: "mal"})
    .onMethod("post").thenReturnSequence("alp", "gulf", "mulf", "lulf");
    string stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "alp");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "gulf");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "mulf");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "lulf");
}

@test:Config {}
public function testArgsWThenReturnSeq() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:integ/:decim/:str")
    .withArguments("bar", 2)
    .onMethod("post").thenReturnSequence("alp", "gulf", "mulf", "lulf");
    string stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "alp");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "gulf");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "mulf");
    stringResult = clientEndpoint->/greeting/hello2/[1]/[2]/["mal"].post(a = "bar", b = 2);
    test:assertEquals(stringResult, "lulf");
}

@test:Config {}
public function testNegWMultiPathParamsWMultiArg_1() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name/:town")
    .withPathParameters({town: "Jaffna", name: "Luhee"})
    .withArguments("bar", "str").onMethod("post").thenReturn("Hello, Luhee");
}

@test:Config {}
public function testWMultiPathParamsWErrorneousMultiArg() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name/:town")
    .withPathParameters({town: "Jaffna", name: "Luhee"})
    .withArguments("bar", "str", "mad").onMethod("post").thenReturn("Hello, Luhee");
}

@test:Config {}
public function testWMultiPathParamsWErrorneousMultiArg_1() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hellore/:name").withPathParameters({name: 2})
    .withArguments("bar").onMethod("get").thenReturn("Hello, Luhee");
}

@test:Config {}
public function testWErrorneousMultiPathParamsWMultiArg() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hellore/:name")
    .withPathParameters({name: "Luhee"})
    .withArguments(2).onMethod("get").thenReturn("Hello, Luhee");
}

@test:Config {}
public function testNegWMultiPathParamsWMultiArg_2() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name/:town")
    .withPathParameters({town: 2, name: "Luhee"})
    .withArguments("bar", 2).onMethod("post").thenReturn("Hello, Luhee");
}

@test:Config {}
public function testWErrorneousMultiPath() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name/:town")
    .withPathParameters({name: "Luhee", towne: "Jaffna"}).withArguments("bar", 2)
    .onMethod("post").thenReturn("Hello, Luhee");
}

@test:Config {}
public function testWErrorneousResourceMethod() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello2/:name/:town")
    .withPathParameters({name: "Luhee", town: "Jaffna"})
    .withArguments("bar", 2).onMethod("postr").thenReturn("Hello, Luhee");
}

@test:Config {}
public function testRemoteMethod() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).when("alp").thenReturn("Hello, Luhee");
    string alp = clientEndpoint->alp();
    test:assertEquals(alp, "Hello, Luhee");
}

@test:Config {}
public function testRestPathParamThenReturn() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello/:num/:str/::comtype").withPathParameters({
        num:
        1,
        str: "Luhee",
        comtype: ["comtype1", "comtype2", "comtype3"]
    }).withArguments("bar", 2).onMethod("get").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello/[1]/["Luhee"]/["comtype1"]/["comtype2"]/
    ["comtype3"].get("bar", 2);
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testThenReturnWOArgsPaths_1() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello/:num/:str/::comtype")
    .thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/hello/[1]/["Luhee"]/["comtype1"]/["comtype2"]/
    ["comtype3"].get("bar", 2);
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testThenReturnWOArgsPaths_2() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/:num/:str/:comtype").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greeting/[1]/["Luhee"]/["comtype1"].get("bar", 2);
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testDefaultArgsResources() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greetin/:al").thenReturn("Hello, Luhee");
    string stringResult = clientEndpoint->/greetin/["Luhee"].get();
    test:assertEquals(stringResult, "Hello, Luhee");
}

@test:Config {}
public function testRestPathParamThenReturnSeq() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("greeting/hello/:num/:str/::comtype")
    .onMethod("get").thenReturnSequence("Hello, Luhee", "Hello, Luhe", "Hello, Lu");
    string stringResult = clientEndpoint->/greeting/hello/[1]/["Luhee"]/["comtype1"]/["comtype2"]/
    ["comtype3"].get("bar", 2);
    test:assertEquals(stringResult, "Hello, Luhee");
    stringResult = clientEndpoint->/greeting/hello/[1]/["Luhee"]/["comtype1"]/["comtype2"]/
    ["comtype3"].get("bar", 2);
    test:assertEquals(stringResult, "Hello, Luhe");
    stringResult = clientEndpoint->/greeting/hello/[1]/["Luhee"]/["comtype1"]/["comtype2"]/
    ["comtype3"].get("bar", 2);
    test:assertEquals(stringResult, "Hello, Lu");
}

@test:Config {}
function test() {
    clientEndpoint = test:mock(Client);
    test:prepare(clientEndpoint).whenResource("foo/bar/").withArguments("me", "you").onMethod("post").thenReturn("mocked post foo/bar");
    string stringResult = clientEndpoint->/foo/bar.post("me", "you");
    test:assertEquals(stringResult, "mocked post foo/bar");
}

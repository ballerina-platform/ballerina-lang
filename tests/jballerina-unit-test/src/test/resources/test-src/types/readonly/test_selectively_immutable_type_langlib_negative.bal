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

import ballerina/lang.array;
import ballerina/lang.'map as mp;
import ballerina/lang.'table;
import ballerina/lang.value as val;
import ballerina/lang.'xml;

type Details record {
    string name;
    int id;
};

function testInvalidImmutableListUpdateViaLangLibMethod() {
    int[] & readonly a = [1, 2];
    [Details, string...] & readonly b = [{name: "Jo", id: 1985}, "IT"];

    int v1 = a.remove(0);
    array:removeAll(a);
    a.setLength(3);
    _ = array:reverse(b);
    _ = a.sort();
    Details|string v2 = array:pop(b);
    b.push("hello");
    int v3 = array:shift(a);
    b.unshift("world");
}

function testInvalidImmutableMapUpdateViaLangLibMethod() {
    map<string> & readonly a = {a: "hello", b: "world"};
    Details & readonly b = {name: "Jo", id: 1985, "dept": "IT"};

    string v1 = a.remove("b");
    anydata v2 = mp:removeIfHasKey(b, "dept");
    mp:removeAll(a);
    b.removeAll();
}

function testInvalidImmutableTableUpdateViaLangLibMethod() {
    table<map<anydata>> & readonly a = table [
        {a: 1, b: "hello"},
        {a: 2, b: 20.0, c: false}
    ];

    table<Details> key(name) & readonly b = table [
        {name: "May", id: 1234},
        {name: "Jo", id: 2345}
    ];

    a.put({a: 1, b: "world", c: true});
    'table:add(b, {name: "Amy", id: 6785});
    Details v1 = b.remove("May");
    Details? v2 = 'table:removeIfHasKey(b, "Amy");
    a.removeAll();
    'table:removeAll(b);
}

function testInvalidImmutableJsonUpdateViaLangLibMethod() {
    json & readonly a = 10;
    map<json> & readonly b = {a: "foo", b: true};

    json|error c = a.mergeJson(b); //  This we allow since `a` may not be updated in the merge process
    json|error d = val:mergeJson(b, a);
    json|error e = b.mergeJson({c: 1});
}

function testInvalidImmutableXmlUpdateViaLangLibMethod() {
    'xml:Element & readonly a = xml `<foo>Foo Element</foo>`;
    xml & readonly b = xml `<!-- I'm a comment -->`;

    a.setName("bar");
    'xml:setChildren(a, b);
    _ = b.strip();
}

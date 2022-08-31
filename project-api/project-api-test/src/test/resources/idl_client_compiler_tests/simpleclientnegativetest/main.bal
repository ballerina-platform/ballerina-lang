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

foo:Config config = 1;
foo:Client cl;

function testModuleClientDeclNegative() {
    foo:ClientConfig config = 1;
    foo:Client cl;
}

function testClientDeclStmtNegative1() {
    client "http://example.com/apis/one.yaml" as bar;
    bar:ClientConfiguratin config;
    bar:clients cl;
}

function testClientDeclStmtNegative2() {
    client "http://example.com/apis/one.yaml" as bar;
    client "http://example.com/apis/two.yaml" as baz;
    baz:ClientConfiguration c1;
    bar:Config c2;
}

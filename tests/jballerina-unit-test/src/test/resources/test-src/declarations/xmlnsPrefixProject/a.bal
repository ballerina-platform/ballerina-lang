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

xmlns "http://exampleA1.com" as ns1;
final string AA = ns1:doc;

function testXMLNSUsage() {
    assert(AA, "{http://exampleA1.com}doc");
    assert(ns1:foo, "{http://exampleA1.com}foo");
    assert(ns3:foz, "{http://exampleA3.com}foz");

    xmlns "http://exampleA2.com" as ns2;
    assert(ns2:foo, "{http://exampleA2.com}foo");
}

xmlns "http://exampleA3.com" as ns3;

function assert(anydata actual, anydata expected) {
    if expected != actual {
        panic error(string `expected ${expected.toString()}, but found ${actual.toString()};`);
    }
}

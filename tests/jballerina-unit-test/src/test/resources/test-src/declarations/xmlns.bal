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

const A = "http://ballerina.com/a";
const string|int B = "http://ballerina.com/b";
const C = "http://ballerina.com/c";
const D = C;
const E = "http://ballerina.com/e";
const string|int F = "http://ballerina.com/f";

xmlns A as ns0;
xmlns xml:XMLNS_NAMESPACE_URI as ns1;
xmlns B as ns2;
xmlns D as ns3;

final string f1 = ns1:lmn;
final string f2 = ns2:ijk;
string s = ns3:pqr;

function testXMLNSDeclUsingConstant() {
    assert(ns0:foo, "{http://ballerina.com/a}foo");
    assert(ns1:foo, "{http://www.w3.org/2000/xmlns/}foo");
    assert(ns2:bar, "{http://ballerina.com/b}bar");
    assert(ns3:bar, "{http://ballerina.com/c}bar");
}

function testXMLNSUsageInModuleVar() {
    assert(f1, "{http://www.w3.org/2000/xmlns/}lmn");
    assert(f2, "{http://ballerina.com/b}ijk");
    assert(s, "{http://ballerina.com/c}pqr");
}

function testXMLNSDeclStmtUsingConstant() {
    xmlns E as ns4;
    assert(ns4:baz, "{http://ballerina.com/e}baz");

    xmlns xml:XMLNS_NAMESPACE_URI as ns5;
    assert(ns5:baz, "{http://www.w3.org/2000/xmlns/}baz");

    xmlns F as ns6;
    assert(ns6:abc, "{http://ballerina.com/f}abc");
}

function assert(anydata actual, anydata expected) {
    if expected != actual {
        panic error("expected `" + expected.toString() + "`, but found `" + actual.toString() + "`");
    }
}

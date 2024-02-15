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
const I = "http://ballerina.com/i"; 

xmlns A as ns0;
xmlns xml:XMLNS_NAMESPACE_URI as ns1;
xmlns B as ns2;
xmlns D as ns3;

final string f1 = ns1:lmn;
final string f2 = ns2:ijk;
final string f3 = ns7:ghi;
final string f4 = ns8:jkl;
string s = ns3:pqr;
string s2 = ns7:sty;
string s3 = ns8:xyz;

type Rec record {|
    string a = ns1:baz;
|};

function testXMLNSDeclUsingConstant() {
    assert(ns0:foo, "{http://ballerina.com/a}foo");
    assert(ns1:foo, "{http://www.w3.org/2000/xmlns/}foo");
    assert(ns2:bar, "{http://ballerina.com/b}bar");
    assert(ns3:bar, "{http://ballerina.com/c}bar");
    Rec rec = {};
    assert(rec.a, "{http://www.w3.org/2000/xmlns/}baz");
    assert(ns7:abc, "{http://ballerina.com/g}abc");
    assert(ns8:def, "{http://ballerina.com/i}def");
}

function testXMLNSUsageInModuleVar() {
    assert(f1, "{http://www.w3.org/2000/xmlns/}lmn");
    assert(f2, "{http://ballerina.com/b}ijk");
    assert(f3, "{http://ballerina.com/g}ghi");
    assert(f4, "{http://ballerina.com/i}jkl");
    assert(s, "{http://ballerina.com/c}pqr");
    assert(s2, "{http://ballerina.com/g}sty");
    assert(s3, "{http://ballerina.com/i}xyz");
}

function testXMLNSDeclStmtUsingConstant() {
    xmlns E as ns4;
    assert(ns4:baz, "{http://ballerina.com/e}baz");

    xmlns xml:XMLNS_NAMESPACE_URI as ns5;
    assert(ns5:baz, "{http://www.w3.org/2000/xmlns/}baz");

    xmlns F as ns6;
    assert(ns6:abc, "{http://ballerina.com/f}abc");
}

xmlns "http://ballerina.com/g" as ns7;
xmlns I as ns8;

function assert(anydata actual, anydata expected) {
    if expected != actual {
        panic error("expected `" + expected.toString() + "`, but found `" + actual.toString() + "`");
    }
}

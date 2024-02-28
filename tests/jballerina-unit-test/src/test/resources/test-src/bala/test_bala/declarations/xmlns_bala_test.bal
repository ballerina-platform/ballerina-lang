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

import testorg/foo;

xmlns foo:XMLNSA as ns0;
xmlns foo:XMLNSB as ns1;

function testXMLNSDeclUsingModuleConstant() {
    assert(ns0:foo, "{http://ballerina.com/a}foo");
    assert(ns1:foo, "{http://ballerina.com/b}foo");
}

function testXMLNSDeclStmtUsingModuleConstant() {
    xmlns foo:XMLNSC as ns2;
    assert(ns2:baz, "{http://ballerina.com/c}baz");
}

function assert(anydata actual, anydata expected) {
    if expected != actual {
        panic error("expected `" + expected.toString() + "`, but found `" + actual.toString() + "`");
    }
}

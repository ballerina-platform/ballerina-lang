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

xmlns "http://ballerinalang.org" as ns;

function testXMLAttributeAccessWithModulePrefix() {
    xml x = xml `<a ns:a="content" ns:B="nsB" b="identity"></a>`;
    assertEquality(x.ns:a, "content");
    assertEquality(x.foo:XMLA, "content");
    assertEquality(x.foo:XMLB, "identity");
}

function testOptionalXMLAttributeAccessWithModulePrefix() {
    xml x = xml `<a ns:a="content" ns:B="nsB" b="identity"></a>`;
    assertEquality(x?.ns:a, "content");
    assertEquality(x?.foo:XMLA, "content");
    assertEquality(x?.foo:XMLB, "identity");
    assertEquality(x?.foo:XMLC, ());
    assertEquality(x?.foo:XMLF, ());
}

function testXMLAttributeAccessErrors() {
    xml x = xml `<a ns:A="content" id="identity"></a>`;
    assertError(x.foo:XMLF, "{ballerina/lang.xml}XMLOperationError", "attribute '{http://ballerinalang.org}f' not found");
    assertError(x.foo:XMLC, "{ballerina/lang.xml}XMLOperationError", "attribute 'c' not found");
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("Assertion Error",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

type Error error<record { string message; }>;

function assertError(any|error value, string errorMessage, string expDetailMessage) {
    if value is Error {
        if (value.message() != errorMessage) {
            panic error("Expected error message: " + errorMessage + " found: " + value.message());
        }

        if value.detail().message == expDetailMessage {
            return;
        }
        panic error("Expected error detail message: " + expDetailMessage + " found: " + value.detail().message);
    }
    panic error("Expected: Error, found: " + (typeof value).toString());
}

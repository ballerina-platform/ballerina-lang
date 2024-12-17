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

function testXmlRequiredAttributeAccessWithModulePrefix() {
    xml x = xml `<a ns:a="content" ns:B="nsB" b="identity"></a>`;
    assertNonErrorValue("content", x.ns:a);
    assertNonErrorValue("content", x.foo:XMLA);
    assertNonErrorValue("identity", x.foo:XMLB);
}

function testXmlOptionalAttributeAccessWithModulePrefix() {
    xml x = xml `<a ns:a="content" ns:B="nsB" b="identity"></a>`;
    assertNonErrorValue("content", x?.ns:a);
    assertNonErrorValue("content", x?.foo:XMLA);
    assertNonErrorValue("identity", x?.foo:XMLB);
    assertNonErrorValue((), x?.foo:XMLC);
    assertNonErrorValue((), x?.foo:XMLF);
}

function testXmlAttributeAccessErrors() {
    xml x = xml `<a ns:A="content" id="identity"></a>`;
    assertError(x.foo:XMLF, "{ballerina/lang.xml}XMLOperationError",
            "attribute '{http://ballerinalang.org}f' not found");
    assertError(x.foo:XMLC, "{ballerina/lang.xml}XMLOperationError", "attribute 'c' not found");
}

function assertNonErrorValue(anydata expected, any|error actual) {
    if actual is error {
        panic error("expected [" + expected.toString() + "] " + ", but got error with message " + actual.message());
    }

    if expected != actual {
        panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
    }
}

type Error error<record {string message;}>;

function assertError(any|error value, string errorMessage, string expDetailMessage) {
    if value is Error {
        string actualErrorMessage = value.message();
        if actualErrorMessage != errorMessage {
            panic error("Expected error message: " + errorMessage + " found: " + actualErrorMessage);
        }
        string actualDetailMessage = value.detail().message;
        if actualDetailMessage == expDetailMessage {
            return;
        }
        panic error("Expected error detail message: " + expDetailMessage + " found: " + actualDetailMessage);
    }
    panic error("Expected: Error, found: " + (typeof value).toString());
}

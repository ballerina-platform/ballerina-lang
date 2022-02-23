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

import testorgthree/isolation;

isolated class IsolatedClassWithIsolatedTypeInclusion {
    *isolation:IsolatedObjectType;

    public final int i = 321;
    public final boolean j = false;
    private string k = "str";

    isolated function getK() returns string {
        lock {
            return self.k;
        }
    }
}

isolated function testIsolatedFunctionCallInIsolatedFunction() {
    int a = isolation:isolatedFunction();
    assertEquality(200, a);

    isolation:IsolatedClassWithExplicitIsolatedInit b = new (new IsolatedClassWithIsolatedTypeInclusion());
    assertEquality(1, b.i);
    assertEquality(321, b.getJ().i);
    IsolatedClassWithIsolatedTypeInclusion c = <IsolatedClassWithIsolatedTypeInclusion> b.getJ();
    assertEquality(false, c.j);
    assertEquality("str", c.getK());

    isolation:IsolatedClassWithoutInit d = new;
    assertEquality(10, d.i);
    assertEquality(20, d.getJ().i);
    assertEquality(true, d.getJ().j);
    assertEquality("hello", d.getK());
}

isolated class IsolatedClassWithImportedIsolatedObjectFields {
    final isolation:IsolatedObjectType a;
    final isolation:IsolatedClassWithExplicitIsolatedInit b = new (new IsolatedClassWithIsolatedTypeInclusion());
    final isolation:IsolatedClassWithoutInit c = new;
    private isolation:NonIsolatedClass d = new;

    function init(isolation:IsolatedObjectType a) {
        self.a = a;
    }
}

function testNewingIsolatedClassWithImportedIsolatedObjectFields() {
    IsolatedClassWithImportedIsolatedObjectFields _ = new (new IsolatedClassWithIsolatedTypeInclusion());
}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;
import ballerina/test;

public type Address record {
    string city;
    string country;
    int postalCode;
};

public function getRecord(string recordName) returns record{} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

type Details record {
    string name;
    int id;
};

type Student record {|
    string name;
|};

public type Foo record {
    int[] y;
};

public type Bar readonly & record {|
    int[] x;
|};

public function validateAPI() {
    anydata recordVal1 = getRecordValue();
    test:assertTrue(recordVal1 is Student);
    test:assertEquals(recordVal1, {"name": "nameOfStudent"});

    Details recordVal2 = getRecordValueWithInitialValues();
    test:assertEquals(recordVal2, {"name": "studentName", "id": 123});

    json jsonValue = {name: "Jane", id: 234};
    anydata recordVal3 = getRecordValueFromJson(jsonValue, Details);
    test:assertEquals(recordVal3, {"name": "Jane", "id": 234});
    test:assertTrue(recordVal3 is Details);
}

function getRecordValue() returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getRecordValueWithInitialValues() returns Details = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getRecordValueFromJson(json value, typedesc<Details> recType) returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getRecordNegative(string recordName) returns record{} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getRecordNegative2(string recordName) returns record{} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getReadonlyRecordNegative(string recordName) returns record{} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getRecordWithRestFieldsNegative() returns record{} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

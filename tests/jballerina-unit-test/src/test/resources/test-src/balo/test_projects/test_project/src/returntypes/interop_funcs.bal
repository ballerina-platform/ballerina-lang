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

import ballerina/java;

//type ItemType 'xml:Element|'xml:Comment|'xml:ProcessingInstruction|'xml:Text;

public type Person record {
    readonly string name;
    int age;
};

public type Employee record {
    *Person;
    string designation;
};

public function getValue(typedesc<int|float|decimal|string|boolean> td) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc"]
} external;

public function getRecord(typedesc<anydata> td = Person) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getRecord",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc"]
} external;

public function query(string q, typedesc<anydata> rowType = int) returns map<rowType> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "query",
    paramTypes: ["org.ballerinalang.jvm.api.values.BString", "org.ballerinalang.jvm.api.values.BTypedesc"]
} external;

public function getTuple(typedesc<int|string> td1, typedesc<record {}> td2, typedesc<float|boolean> td3 = float) returns [td1, td2, td3] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTuple",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BTypedesc"]
} external;

public function getVariedUnion(int x, typedesc<int|string> td1, typedesc<record{ string name; }> td2) returns (td1|td2) = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getVariedUnion",
    paramTypes: ["long", "org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BTypedesc"]
} external;

public function getArray(typedesc<anydata> td) returns td[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getArray",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc"]
} external;

public function getInvalidValue(typedesc<int|Person> td1, typedesc<Person> td2) returns td1 = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getInvalidValue",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BTypedesc"]
} external;

//public function getXML(typedesc<ItemType> td, xml value) returns xml<td> = @java:Method {
//    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
//    name: "getXML",
//    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BXML"]
//} external;

public function getStream(typedesc<anydata> td, stream<anydata> value) returns stream<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getStream",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BStream"]
} external;

public function getTable(typedesc<anydata> td, table<anydata> value) returns table<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTable",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.values.TableValue"]
} external;

public function getFunction(typedesc<anydata> param, typedesc<anydata> ret, function (string|int) returns anydata fn)
                                                                returns function (param) returns ret = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFunction",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BTypedesc",
                    "org.ballerinalang.jvm.api.values.BFunctionPointer"]
} external;

public function getTypedesc(typedesc<anydata> td) returns typedesc<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTypedesc",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc"]
} external;

public function getFuture(typedesc<anydata> td, future<anydata> f) returns future<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFuture",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BFuture"]
} external;

public function echo(typedesc<any> td, any val) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "echo",
    paramTypes: ["org.ballerinalang.jvm.api.values.BTypedesc", "org.ballerinalang.jvm.api.values.BValue"]
} external;

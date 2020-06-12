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

public function getValue(public typedesc<int|float|decimal|string|boolean> td) returns td = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

public function getRecord(public typedesc<anydata> td = Person) returns td = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getRecord",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

public function query(string q, public typedesc<anydata> rowType = int) returns map<rowType> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "query",
    paramTypes: ["org.ballerinalang.jvm.values.api.BString", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

public function getTuple(public typedesc<int|string> td1, public typedesc<record {}> td2, public typedesc<float|boolean> td3 = float) returns [td1, td2, td3] = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTuple",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

public function getVariedUnion(int x, public typedesc<int|string> td1, public typedesc<record{ string name; }> td2) returns (td1|td2) = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getVariedUnion",
    paramTypes: ["long", "org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

public function getArray(public typedesc<anydata> td) returns td[] = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getArray",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

public function getInvalidValue(public typedesc<int|Person> td1, public typedesc<Person> td2) returns td1 = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getInvalidValue",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

//public function getXML(public typedesc<ItemType> td, xml value) returns xml<td> = @java:Method {
//    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
//    name: "getXML",
//    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BXML"]
//} external;

public function getStream(public typedesc<anydata> td, stream<anydata> value) returns stream<td> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getStream",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BStream"]
} external;

public function getTable(public typedesc<anydata> td, table<anydata> value) returns table<td> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTable",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.TableValue"]
} external;

public function getFunction(public typedesc<anydata> param, public typedesc<anydata> ret, function (string|int) returns anydata fn)
                                                                returns function (param) returns ret = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFunction",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BTypedesc",
                    "org.ballerinalang.jvm.values.api.BFunctionPointer"]
} external;

public function getTypedesc(public typedesc<anydata> td) returns typedesc<td> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTypedesc",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

public function getFuture(public typedesc<anydata> td, future<anydata> f) returns future<td> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFuture",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BFuture"]
} external;

public function echo(public typedesc<any> td, any val) returns td = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "echo",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BValue"]
} external;

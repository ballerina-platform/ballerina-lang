// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type ErrorRec record {|
    int id;
    string msg;
    PositiveInt...;
|};

type Detail ErrorRec;

type DetailedError error<Detail>;

type FunctionType function (PositiveInt a, PositiveInt... args) returns PositiveInt;

type IntersectionType Detail & readonly;

type MapType map<PositiveInt>;

type RecordType ErrorRec;

type StreamType stream<PositiveInt, PositiveInt>;

type TableType table<Detail>;

type TupleType [PositiveInt, Detail, PositiveIntRef...];

type TypedescType typedesc<PositiveInt>;

type UnionType PositiveInt|Detail|PositiveIntRef;

public class Test {
    int i = 1;

    public function get(typedesc<PositiveInt> td) returns td|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference",
        name: "getInt",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

function validateRuntimeAPIs() {
    boolean result = validateGetDetailType(DetailedError);
    test:assertTrue(result);

    result = validateFunctionType(FunctionType);
    test:assertTrue(result);

    result = validateIntersectionType(IntersectionType);
    test:assertTrue(result);

    result = validateMapType(MapType);
    test:assertTrue(result);

    result = validateRecordType(RecordType);
    test:assertTrue(result);

    result = validateStreamType(StreamType);
    test:assertTrue(result);

    result = validateTableType(TableType);
    test:assertTrue(result);

    result = validateTupleType(TupleType);
    test:assertTrue(result);

    result = validateTypedescType(TypedescType);
    test:assertTrue(result);

    result = validateUnionType(UnionType);
    test:assertTrue(result);

    Test testVal = new ();
    result = validateParameterizedType(testVal);
    test:assertTrue(result);

    result = validateTypeUtilsAPI(PositiveInt);
    test:assertTrue(result);
}

public function validateGetDetailType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateFunctionType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateIntersectionType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateMapType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateRecordType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateStreamType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateTableType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateTupleType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateTypedescType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateUnionType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateParameterizedType(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateTypeUtilsAPI(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;



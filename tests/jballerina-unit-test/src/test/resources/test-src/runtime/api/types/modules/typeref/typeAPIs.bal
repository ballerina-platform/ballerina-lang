// Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import ballerina/jballerina.java;
import ballerina/test;

type ErrorRec record {|
    readonly PositiveInt id;
    string msg;
    PositiveInt...;
|};

type Detail ErrorRec;

type DetailedError error<Detail>;

type FunctionType function (PositiveInt a, PositiveInt... args) returns PositiveInt;

type IntersectionType Detail & readonly;

type MapType map<PositiveInt>;

type RecordType ErrorRec;

type ErrorOrNil DetailedError?;

type StreamType stream<PositiveInt, ErrorOrNil>;

type TableType table<Detail> key(id);

type TupleType [PositiveInt, Detail, PositiveIntRef...];

type TypedescType typedesc<PositiveInt>;

type UnionType PositiveInt|Detail|PositiveIntRef;

type BArray PositiveInt[];

type BTuple [PositiveInt, Detail];

type BFunctionPointer function (int a) returns int;

type BMap map<PositiveInt>;

public class Test {
    int i = 1;

    public function get(typedesc<PositiveInt> td) returns td|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference",
        name: "getInt",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

class PositiveNumberGenerator {
    PositiveInt i = 0;

    public isolated function next() returns record {|PositiveInt value;|}|ErrorOrNil {
        self.i += 2;
        return {value: self.i};
    }
}

type BObject Test;

public class TestWithInit {
    int i = 1;

    function init() {
        self.i = 5;
    }
}

public class TestWithInitReturnError {
    int i = 1;

    function init() returns error? {
        self.i = 5;
    }
}

public class TestWithInitReturnTypeRefError {
    int i = 1;

    function init() returns DetailedError? {
        self.i = 5;
    }
}

type BObjectWithInit TestWithInit;

type BObjectWithInitReturnError TestWithInitReturnError;

type BObjectWithInitReturnTypeRefError TestWithInitReturnTypeRefError;

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

    PositiveNumberGenerator gen = new ();
    StreamType s = new (gen);
    result = validateStreamType(StreamType, s);
    test:assertTrue(result);

    TableType tab = table [];
    result = validateTableType(TableType, tab);
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

    BArray arr = [1, 2, 3];
    BTuple tup = [1, {id: 101, msg: "message", "priority": 2}];
    result = validateBArray(arr, tup);
    test:assertTrue(result);

    BMap m = {a: 1, b: 2, c: 3};
    RecordType r = {id: 11, msg: "message", "intVal": 22};
    result = validateBMap(m, r);
    test:assertTrue(result);

    DetailedError err = error("This is error", id = 101, msg = "error message");
    result = validateBError(err);
    test:assertTrue(result);

    BObject obj = new ();
    result = validateBObject(obj);
    test:assertTrue(result);

    BObjectWithInit obj2 = new ();
    result = validateBObject(obj2);
    test:assertTrue(result);

    BObjectWithInitReturnError obj3 = checkpanic new ();
    result = validateBObject(obj3);
    test:assertTrue(result);

    BObjectWithInitReturnTypeRefError obj4 = checkpanic new ();
    result = validateBObject(obj4);
    test:assertTrue(result);

    BFunctionPointer fp = testFunc;
    result = validateBFunctionPointer(fp);
    test:assertTrue(result);

    fp = (i) => 5 * i;
    result = validateBFunctionPointer(fp);
    test:assertTrue(result);
}

function testFunc(int a) returns int {
    return a + 5;
}

type UnionValues BArray|BTuple|BMap|TableType|BObject;

function validateValueWithUnion() {
    UnionValues value = [2, 4, 6, 8];
    boolean result = validateUnionTypeNarrowing(value, BArray);
    test:assertTrue(result);

    value = [1, {id: 100, msg: "hello", "priority": 5}];
    result = validateUnionTypeNarrowing(value, BTuple);
    test:assertTrue(result);

    value = {"a": 1, "b": 2, "c": 3};
    result = validateUnionTypeNarrowing(value, BMap);
    test:assertTrue(result);

    value = table [{id: 200, msg: "error message", "priority": 1}];
    result = validateUnionTypeNarrowing(value, TableType);
    test:assertTrue(result);

    value = new ();
    result = validateUnionTypeNarrowing(value, BObject);
    test:assertTrue(result);
}


type Sample record {
    readonly int id;
    readonly int depId;
    string name;
};

type RecordRef Sample;

type RecordRef2 RecordRef;

type RecordTable table<RecordRef2> key(id, depId);

function validateTableMultipleKey() {
    RecordTable recTab = table [{id: 101, depId: 99, name: "aaa"}];
    boolean result = validateTableKeys(recTab);
    test:assertTrue(result);
}

function validateTableKeys(any value) returns boolean = @java:Method { 
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

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

public function validateStreamType(any value1, any value2) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateTableType(any value1, any value2) returns boolean = @java:Method {
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

public function validateBArray(any value1, any value2) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateBMap(any value1, any value2) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateBError(any|error value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateBObject(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateBFunctionPointer(any value) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

public function validateUnionTypeNarrowing(any|error value, typedesc<any> td) returns boolean = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.TypeReference"
} external;

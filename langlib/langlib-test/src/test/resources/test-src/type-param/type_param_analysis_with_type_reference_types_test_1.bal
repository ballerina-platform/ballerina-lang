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

function testFn() {
    Value[] vArr = [];
    vArr.push(get());

    ConstValue c = <ConstValue> vArr[0];
    assertEquality("val", c.ty);
}

function get() returns ConstValue {
    return new ConstValue("val");
}

type Value DataValue;

public readonly distinct class DataValue {
    Type ty;
    function init(Type ty) {
        self.ty = ty;
    }
}

public readonly class ConstValue {
    *DataValue;
    Type ty;
    function init(Type ty) {
        self.ty = ty;
    }
}

public type Type string;

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString()  + "'");
}

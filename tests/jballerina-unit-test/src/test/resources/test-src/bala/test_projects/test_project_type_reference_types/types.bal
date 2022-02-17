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

public type Foo record {
    Bar a;
};

public type Bar record {
    Baz b;
};

public type Baz record {
    Foo|Bar c?;
};

public type Qux int?;

public type Quux Qux;

public type Int int;

public type Corge Int;

public type Integer int;

public type Decimal decimal;

public type Strings string;

public type IntArray int[2];

public type FloatBooleanTuple [float, boolean];

public type Record record {|
    FloatBooleanTuple a;
    () b;
|};

public type FunctionType2 function (FloatBooleanTuple i) returns Record;

public type ImmutableIntArray int[] & readonly;

public type FooBar "foo"|1;

public const int ConstInt = 5;

type Integer2 int;

type Decimal2 decimal;

type Strings2 string;

type IntArray2 int[2];

type FloatBooleanTuple2 [float, boolean];

type Record2 record {|
    FloatBooleanTuple2 a;
    () b;
|};

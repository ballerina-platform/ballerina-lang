// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com).
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

const string[2] ERR_ARR1 = [3, 1]; // incompatible types
const int[1] ERR_ARR2 = [3, 1]; // incompatible types, size mismatch
const (float|decimal)[] ERR_ARR3 = [3, 2.0 + 1d]; // incompatible types
const int[] ERR_ARR4 = [1, 2, getIntValue()]; // expression is not a constant expression

function getIntValue() returns int {
    return 3;
}

const TUPLE1 = [3, 1];
const float[] ERR_TUPLE1 = [...TUPLE1]; // incompatible types

const [int, float] ERR_TUPLE2 = [3, 1, 3]; // incompatible types, size mismatch
[record {| int a; 2 b;|}] ERR_TUPLE_3 = []; // does not have a filler value
const [1|2] ERR_TUPLE4 = []; // no fill members
const int[2]|[int, int] ERR_TUPLE5 = []; // ambiguous types
const TUPLE1 TUPLE2 = [3, 3]; // incompatible types
const byte[2] ARR_1 = [300, 200]; // incompatible types
const byte[2] ARR_2 = [300, 200, 100]; // size mismatch
const byte[*] ARR_3 = [300, 400]; // incompatible types
const [byte, byte, int:Signed8] TUPLE_3 = [300, 400, 500]; // incompatible types

const float[]|decimal[] ARR_5 = [1, 2.0]; // ambiguous types
const string[]|boolean[] ARR_6 = [1, 2.0]; // incompatible types
const int|float ARR_7 = [1, 2.0]; // incompatible types
const int ARR_8 = [1, 2.0]; // incompatible types
const TUPLE_4 = [2 > 3]; // const expressions are not yet supported here
const [record {| int a = 5; 2 b;|}] TUPLE_5 = [{b: 2}];
const [int, table<int>] TUPLE_6 = [1]; // incompatible types

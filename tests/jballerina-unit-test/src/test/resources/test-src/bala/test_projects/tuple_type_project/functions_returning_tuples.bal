// Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

public const annotation member on field;
public const annotation tup on type;

type T1 [int, @member int, string...];
type T2 [int, @member int, string];
type T3 [@member int, string];

@tup
type T4 [@member int, string];

public function returnTupleWithMemberAnnot1() returns T1 {
    T1 x1 =  [1, 2, "hello", "world"];
    return x1;
}

public function returnTupleWithMemberAnnot2() returns [T2, T3, T4] {
    T2 x2 =  [1, 2, "a"];
    T3 x3 =  [1, "hello"];
    T4 x4 =  [1, "a"];
    return [x2, x3, x4];
}

public function getTupleWithMemberAndRestDesc() returns [int, string...] {
    return [1, "hello", "world"];
}

public function getTupleWithRestDescOnly() returns [string...] {
    return ["hello", "world"];
}

public function getEmptyTupleWithRestDescOnly() returns [string...] => [];

public function getTupleWithUnionRestDesc() returns [int, (string|boolean)...] => [2, "hello", true, false];

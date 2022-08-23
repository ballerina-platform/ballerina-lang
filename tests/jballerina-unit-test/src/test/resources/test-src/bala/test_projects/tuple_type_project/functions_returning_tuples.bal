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

public type T [int, @member int, string...];

public function returnTupleAnnot() returns T {
    T x =  [1, 2, "hello", "world"];
    return x;
}

public function getTupleWithMemberAndRestDesc() returns [int, string...] {
    return [1, "hello", "world"];
}

public function getTupleWithRestDescOnly() returns [string...] {
    return ["hello", "world"];
}

public function getEmptyTupleWithRestDescOnly() returns [string...] => [];

public function getTupleWithUnionRestDesc() returns [int, (string|boolean)...] => [2, "hello", true, false];

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

public enum MyEnum {
    A,
    B
}

public type MyTuple [int, string];

public function func1() returns MyEnum => A;
public function func2() returns A|B => A;
public function func3() returns MyTuple => [1, ""];
public function func4() returns [int, string] => [1, ""];

public function testName() returns [typedesc<function>, typedesc<function>, typedesc<function>, typedesc<function>] {
    return [typeof func1, typeof func2, typeof func3, typeof func4];
}

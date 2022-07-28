// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

type Ttype typedesc<int|string>;

type Foo object {
    public function testFn(typedesc<anydata> tdA = <>) returns tdA|error;
};

public function testParameterizedReturn() returns error? {
     Example e = new();
     Foo foo = check getFoo();
     foo.testFn();
     e.getTD();
}

function getFoo() returns Foo|error {
    return error("An error");
}

class Example {
 function getTD(Ttype td = <>) returns td = external;
}

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

function test() {
    isolated client object {
        # An object field
        @v1 {tag: "f1"}
        public string name;

        # An object method
        @v1 {tag: "m1"}
        public remote transactional isolated function bar(@v2 string p1, @v2 int p2 = 10, @v2 string... p3);
    } obj;
}

type Person object {
    string name;
    int age;

    public function getName() returns string;

    public function getAge() returns int;
};

type Employee object {
    *Person;
    string designation;
};

type Foo isolated client object {
    # An object field
    @v1 {tag: "f1"}
    public string name;

    # An object method
    @v1 {tag: "m1"}
    public remote transactional isolated function bar(@v2 string p1, @v2 int p2 = 10, @v2 string... p3);
};

// utils

type Annot record {
    string tag;
};

public const annotation Annot v1 on source type, field, var, parameter, function;
public const annotation v2 on source parameter;

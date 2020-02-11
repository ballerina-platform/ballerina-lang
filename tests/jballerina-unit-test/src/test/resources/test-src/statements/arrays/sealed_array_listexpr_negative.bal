// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Person abstract object {
    public int age;
    function getFullName() returns string;
};

// [0] cannot initialize abstract objects
function createAbstractObjectArray() {
    Person[5] x = [];
    Person p1 = x[2];
    p1.age = 20;
}

// [1] cannot create multi dimensional arrays of abstract objects
function createAbstractObjectEmptyArray() {
    Person[5][] y = [];
    Person[5][1] z = [[]];
}

type Age object {
    public int age;
    public function __init(int age) {
    	 self.age = age;
    }
};

// [2] cannot create object without properly initializing it
function createDirtyObjectEmptyArray() {
    Age[5][] y = [];
    Age[5][1] z = [[]];
    z[0][0].age = 30;
}

type AgeDefaulted object {
    public int age;
    public function __init(int age = 5) {
    	 self.age = age;
    }
};

// should not have a problem
function createObjectDefaultedInitializedEmptyArray() {
    AgeDefaulted[5][] y = [];
    AgeDefaulted[5][1] z = [[]];
    z[0][0].age = 30;
}

// [3] invalid union fill
type myVar 1 | 2 | 3 | 4;
function createInvalidUnionInitializedArray() {
    myVar[3] z = [];
}

// [4] invalid union fill
type myNonHomogeneousUnion 0 | 0.0 | "";
function createInvalidNonHomogeneousUnionInitializedArray() {
    myNonHomogeneousUnion[3] z = [];
}

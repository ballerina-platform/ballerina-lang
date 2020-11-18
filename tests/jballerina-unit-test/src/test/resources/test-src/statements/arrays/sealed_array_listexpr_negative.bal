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

type Person object {
    public int age;
    function getFullName() returns string;
};

// cannot initialize abstract objects
function createAbstractObjectArray() {
    Person[5] x = [];
    Person p1 = x[2];
    p1.age = 20;
}

// cannot create multi dimensional arrays of abstract objects
function createAbstractObjectMultidimentionalSealedArray() {
    Person[5][1] z = [[]];
}

class Age {
    public int age;
    public function init(int age) {
    	 self.age = age;
    }
}

// cannot create object without properly initializing it
function createDirtyObjectMultidimentionalSealedArray() {
    Age[5][1] z = [[]];
    z[0][0].age = 30;
}

class AgeDefaulted {
    public int age;
    public function init(int age = 5) {
    	 self.age = age;
    }
}

// should not have a problem
function createObjectWithDefaultInitializerParametersSealedArray() {
    AgeDefaulted[5][1] z = [[]];
    z[0][0].age = 30;
}

// [3] invalid union fill
type myVar 1 | 2 | 3 | 4;
function createInvalidUnionSealedArray() {
    myVar[3] z = [];
}

// [4] invalid union fill
type myNonHomogeneousUnion 0 | 0.0 | "";
function createInvalidNonHomogeneousUnionSealedArray() {
    myNonHomogeneousUnion[3] z = [];
}

type Rec record {
    int i;
};

type RecWithManyOptional record {|
    int j?;
    int k;
    int a?;
|};

type RecWithOptional record {
    int k;
    int a?;
};

function createRecordTypeWithManyRequiredFieldsSealedArrayCreation() {
    Rec[2] x = [];
}

function createRecordTypeWithManyOptionalFieldsSealedArrayCreation() {
    RecWithManyOptional[2] x = [];
}

function createRecordTypeWithOptionalFieldsSealedArrayCreation() {
    RecWithOptional[2] x = [];
}


class ObjError {
    int i;

    function init() returns error? {
        self.i = 1;
    }
}

function createObjectWithErrorTypeReturningInitializerSealedArray() {
    ObjError [2] y = [];
}

const CONST_HELLO = "HELLO";
const CONST_TWO = 2;

type unionWithConst CONST_HELLO | CONST_TWO ;

function createUnionWithConstTypesSealedArray() {
    unionWithConst[2] unionArr = [];
}

function testInvalidUnionExpectedType() {
    int|NoFillerObject[2] y = [];

    NoFillerObject[3]|NoFillerObject[2] z = [];
}

class NoFillerObject {
    public function init(any arg) {

    }
}

const MyFloatConst = 1.0;
const MyIntZeroConst = 0;

type unionWithIntFloatConsts MyIntZeroConst | MyFloatConst ;

function createUnionWithIntFloatConstTypesSealedArray() {
    unionWithIntFloatConsts[2] unionArr = [];
}

const INT_ONE = 1;
const INT_TWO = 2;
type INT_ONE_TWO INT_ONE | INT_TWO;
function createUnionWithIntOneIntTwoConstTypesSealedArray() {
    INT_ONE_TWO[2] unionArr = [];
}

const FOO = "foo";
const BAR = "bar";

type FooBar FOO|BAR;

public function unionOfMapsWithoutProperInherantTypeForEmptyMapping() {
    (map<FooBar>|map<string>)[2] x = [];
}

// same type unions
type LiteralConstAndIntType int|CONST_TWO; // We currently allow this

function testLiteralConstAndIntType() {
    LiteralConstAndIntType[2] x = [];
}

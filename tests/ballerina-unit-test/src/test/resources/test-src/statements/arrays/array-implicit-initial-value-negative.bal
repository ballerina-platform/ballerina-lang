// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type RR record {
    int i;
};

type Obj object {
    int i;

    function __init() {
        self.i = 0;
    }
};

type ObjInitWithParam object {
    int i;

    function __init(int i) {
        self.i = i;
    }
};

type NoFieldObj object {
   function foo() {
       // pass
   }
};

type RecordWithObj record {
   *RR;
    Obj obj;
};

// Obj have an implicit initial value, this is valid
Obj[] objArray = [];
Obj[][] multiDimObjArray = [];

// ObjInitWithParam doesn't have an implicit initial value, this is invalid
ObjInitWithParam[] objWithParamsArray = [];

// ObjInitWithParam? have an implicit initial value, this is valid
ObjInitWithParam?[] objWithParamsArrayOpt = [];

NoFieldObj[] noFieldObjArray = [];

// Record with all elelements having implicit init value.
RR[] recArray = [];
RR[2] sealedRecArray = [ {i: 0}, {i: 1}];
RR?[] optRecArray = [];
RR[][] multiDimArray = [];

RecordWithObj[] recArrayN = [];
RecordWithObj?[] optRecArrayN = [];

// Json have implicit initial value.
json[] jArr = [];

type FT 1|2|3;
// FT doesn't have an implicit initial value, invalid.
FT[] finiteTypeArray = [1,2];
FT?[] optFiniteTypeArray = [2,3];

type FTN 1|2|3|();
// TFN have an implicit initial value
FTN[] finiteTypeIIVArray = [];

// Union of same type, which does have a IIV.
type IandI int|int;
IandI[] iAndI = [];

type FandF float|float;
FandF[] fandF = [];

type FTUnion FT|FT;
FTUnion[] fU = [];

type FTSingle FT;
FTSingle[] fUS;

type FTNUnion FTN|FTN;
FTNUnion[] ftnU = [];

type FTNSingle FTN;
FTNSingle[] ftnS = [];

error[] errors = [];

function foo(FTN[] ftns) {
    var i = ftns;
}

function bar((function (FT[])) func) {
    _ = func.call([1]);
}

function bar2((function (FTN[])) func) {
    _ = func.call([1]);
}

type UN int|float;
UN[] un = [];

function foo0 () returns int|float {
    return 0;
}

function foo1 () returns UN {
    return 0;
}

function foo2 () returns (int, float) {
    return (0, 0.0);
}

function foo3 () returns (int[], float) {
    return ([1], 1.0);
}

function foo4 () returns int[] {
    return [1];
}

function foo5 (int|float param) {

}

error[]? errorsOpt = [];

// Arrays of fixed length does not have implicit initial value restriction.
var objIniObj = new ObjInitWithParam(0);
ObjInitWithParam[2] objWithParamsArraySealed = [objIniObj, objIniObj];

FT[2] finiteTypeArraySealed = [1,2];

FandF[]|FTUnion[] unionOfArrays = [2.4];

(anydata|error)[] anyOrErrorList = [];

anydata[] anyDataArray = [];
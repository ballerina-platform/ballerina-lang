// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function checkRefEqualityOfTwoTypes() returns boolean {
    int a = 0;
    string b = "";
    return a === b && !(a !== b);
}

function checkRefEqualityOfArraysOfDifferentTypes() returns boolean {
    int[2] a = [0, 0];
    string[2] b = ["", ""];
    boolean bool1 = a === b && !(a !== b);

    (float|int)?[] c = [];
    (boolean|xml)?[] d = [];
    boolean bool2 = c === d && !(c !== d);

    return bool1 && bool2;
}

function checkRefEqualityOfMapsOfIncompatibleConstraintTypes() returns boolean {
    map<int> a = {};
    map<float> b = {};
    boolean bool1 = a === b && !(a !== b);

    map<string|int> c = {};
    map<float> d = {};
    boolean bool2 = c === d && !(c !== d);

    return bool1 && bool2;
}

function checkRefEqualityOfTuplesOfDifferentTypes() returns boolean {
    [string, int] a = ["", 0];
    [boolean, float] b = [false, 0.0];
    boolean bool1 = a === b && !(a !== b);

    [float|int, int] c = [0, 0];
    [boolean, int] d = [false, 0];
    boolean bool2 = c === d && !(d !== c);

    return bool1 && bool2;
}

function checkRefEqualityOfRecordsOfIncompatibleTypes() returns boolean {
    Employee e = { name: "Maryam" };
    Person p = { name: "Maryam" };
    return e === p && !(p !== e);
}

function checkRefEqualityWithJsonForIncompatibleType() returns boolean {
    [string, int] t = ["Hi", 1];
    json j = "Hi 1";
    boolean bool1 = t === j && !(j !== t);

    Employee|[string, int] e = ["Hi", 1];
    j = "Hi 1";
    boolean bool2 = e === j && !(e !== j);

    return bool1 && bool2;
}

function checkRefEqualityOfObjectsOfIncompatibleTypes() returns boolean {
    Abc a = new("abc", 12);
    Def d = new("abc", 12);
    return a === d && !(d !== a);
}

type Employee record {
    string name;
    int id = 0;
};

type Person record {
    string name;
    int area = 0;
};

class Abc {
    public string name;
    private int area;

    public function init(string name, int area) {
        self.name = name;
        self.area = area;
    }
}

class Def {
    public string name;
    private int id;

    public function init(string name, int id) {
        self.name = name;
        self.id = id;
    }
}

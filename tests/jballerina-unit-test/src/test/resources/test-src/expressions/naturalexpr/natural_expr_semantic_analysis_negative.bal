// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.com).
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

int a = natural {
    What is 1 + 1?
};

type PersonObject object {
    string name;
    int age;
};

function f1(string day) returns PersonObject|error => natural {
    Who is a popular person born on ${day}?
};

type Person record {|
    string name;
    int age;
|};

function f2(PersonObject person) returns Person|error => natural {
    Who is a popular person
    born on ${day}? Maybe ${person}?
};

function f3() returns error => natural {
    What's the colour of this IDE?
};

int count = 5;
int[] b = const natural {
    Give me ${count} integers between 1 and 100
};

error c = const natural {
    What's the colour of this IDE?
};

const DAY = "9th April";

PersonObject d = const natural {
    Who is a popular person born on ${DAY}?
};

function f4() returns string|error => natural (mdl) {
    What day is it today?
};

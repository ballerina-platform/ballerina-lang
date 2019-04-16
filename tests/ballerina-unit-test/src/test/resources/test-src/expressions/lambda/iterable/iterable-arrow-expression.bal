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

int index = 0;

function testMapIterable() returns string[] {
    map<string> words = { a: "ant", b: "bear"};
    string[] animals = words.map(word => word[1].toUpper());
    return animals;
}

function testFilterIterable() returns float {
    int[6] numberArray = [5, 6, 7, 8, 9, 10];

    index = 0;

    float avg = numberArray.map(function (int num) returns (int, int) {
        (int, int) value = (index, num);
        index += 1;
        return value;
    }).filter(num => num[0] >= 3).map(num => num[1]).average();
    return avg;
}

function testTwoLevelMapIterable () returns string[] {
    map<string> words = { a: "ant", b: "bear"};
    string[] animals = words.map(entry => (entry[1], entry[1].toUpper())).map(entry => entry[1].toUpper());
    return animals;
}

function testTwoLevelMapIterableWithFilter () returns string[] {
    map<string> words = { a: "ant", b: "bear"};
    string[] animals = words
        .map(entry => (entry[1], entry[1].toUpper()))
        .filter(entry => entry[0] == "bear")
        .map(entry => entry[1]);
    return animals;
}

function testFilterThenMap () returns (string[], int) {
    map<string> words = { a: "ant", b: "bear"};
    string[] str = words.filter(word => word[1] == "ant").map(word => word[1].toUpper() + " MAN");
    int count = words.filter(word => word[1] == "ant").map(word => word[1].toUpper() + " MAN").count();
    return (str, count);
}

function testFilterWithArityOne () returns string[] {
    map<string> words = { a: "ant", b: "bear", c: "tiger"};
    string[] animals = words
        .map(entry => entry[1])
        .filter(entry => entry != "bear")
        .map(entry => entry.toUpper());
    return animals;
}

function testIterableReturnLambda () returns (function (int) returns boolean)?[] {
    map<string> words = { a: "ant", b: "bear", c: "tiger"};
    (function (int) returns boolean)?[] lambdas = words.map(function ((string, string) input) returns (function (int) returns boolean) {
            return param => true;
    });
    return lambdas;
}

function testCountFunction() returns any {
    map<any> numbers = {a: "1", b: "2", c: "3"};
    int v = numbers.map(entry => entry).count();
    return v;
}

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

import ballerina/lang.'int as ints;

int index = 0;

function testMapIterable() returns map<string> {
    map<string> words = { a: "ant", b: "bear"};
    map<string> animals = words.'map(word => word.toUpperAscii());
    return animals;
}

function testFilterIterable() returns float {
    int[6] numberArray = [5, 6, 7, 8, 9, 10];

    index = 0;

    int[] avg = numberArray.map(function (int num) returns [int, int] {
        [int, int] value = [index, num];
        index += 1;
        return value;
    }).filter(num => num[0] >= 3).map(num => num[1]);
    return (<float>(ints:sum(...avg)))/<float>avg.length();
}

function testTwoLevelMapIterable () returns map<string> {
    map<string> words = { a: "ant", b: "bear"};
    map<string> animals = words.map(entry => [entry, entry.toUpperAscii()]).map(entry => entry[1].toUpperAscii());
    return animals;
}

function testTwoLevelMapIterableWithFilter () returns map<string> {
    map<string> words = { a: "ant", b: "bear"};
    map<string> animals = words
        .map(entry => [entry, entry.toUpperAscii()])
        .filter(entry => entry[0] == "bear")
        .map(entry => entry[1]);
    return animals;
}

function testFilterThenMap () returns [map<string>, int] {
    map<string> words = { a: "ant", b: "bear"};
    map<string> str = words.filter(word => word == "ant").map(word => word[1].toUpperAscii() + " MAN");
    int count = words.filter(word => word == "ant").map(word => word[1].toUpperAscii() + " MAN").length();
    return [str, count];
}

function testFilterWithArityOne () returns map<string> {
    map<string> words = { a: "ant", b: "bear", c: "tiger"};
    map<string> animals = words
        .map(entry => entry)
        .filter(entry => entry != "bear")
        .map(entry => entry.toUpperAscii());
    return animals;
}

function testIterableReturnLambda () returns map<(function (int) returns boolean)> {
    map<string> words = { a: "ant", b: "bear", c: "tiger"};
    map<(function (int) returns boolean)> lambdas = words.map(function (string input) returns (function (int) returns boolean) {
            return param => true;
    });
    return lambdas;
}

function testCountFunction() returns any {
    map<any> numbers = {a: "1", b: "2", c: "3"};
    int v = numbers.map(entry => entry).length();
    return v;
}

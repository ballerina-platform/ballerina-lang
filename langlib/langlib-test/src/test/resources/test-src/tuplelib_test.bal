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
import ballerina/lang.array;
import ballerina/lang.test;

function testEnumerate() returns [int, (int|string|float)][] {
    [int, string, int, float] person = [1, "John Doe", 25, 5.9];
    return person.enumerate();
}

function testLength() returns int {
    [int, string, int, float] person = [1, "John Doe", 25, 5.9];
    return person.length();
}

function testMap() returns (int|string|float)[] {
    [int, string, int, float] person = [1, "John Doe", 25, 5.9];

    (int|string|float)[] newArr = person.'map(function (int|string|float x) returns int|string|float {
        if (x is int) {
            return x * 10;
        } else if (x is string) {
            return "mapped " + x;
        } else {
            return x * 5.0;
        }
    });

    return newArr;
}

function testForeach() returns int {
    [int, string, float, map<string>] tup = [10, "Foo", 12.34, {"k":"Bar"}];
    int result = 0;

    tup.forEach(function (string|int|map<string>|float x) {
        if (x is int) {
            result += 10;
        } else if (x is string) {
            result += x.length();
        } else if (x is float) {
            result += <int>x;
        } else {
            result += x.length();
        }
    });

    return result;
}

function testSlice() returns (int|string|float|boolean|map<string>)[] {
    [int, string, float, boolean, map<string>] tup = [10, "Foo", 12.34, true, {"k":"Bar"}];
    return tup.slice(1, 4);
}

function testRemove() {
    [int, string, float] tup = [10, "foo", 12.34];
    var elem = tup.remove(1);
}

function testSort(){
    [int, int, int, int, int, int] arr = [98, 34, 44, 87, 13, 2];
    var sortFunc = isolated function(int x) returns int {
       return x;
    };
    int[] sorted = arr.sort(array:ASCENDING, sortFunc);
}

function testReduce() returns float {
    [int, int, int, int, int] tup = [12, 15, 7, 10, 25];
    float avg = tup.reduce(function (float accum, int val) returns float {
        return accum + <float>val / <float>tup.length();
    }, 0.0);
    return avg;
}

type Grade "A+"|"A"|"A-"|"B+"|"B"|"B-"|"C"|"F";

function testIterableOpChain() returns float {
    [[Grade, int], [Grade, int], [Grade, int], [Grade, int]] grades = [["A+", 2], ["A-", 3], ["B", 3], ["C", 2]];

    int totalCredits = grades.reduce(sum, 0);

    float gpa = grades.'map(gradeToValue).reduce(function (float accum, [float, int] gradePoint) returns float {
        return accum + (gradePoint[0] * <float>gradePoint[1]) / <float>totalCredits;
    }, 0.0);

    return gpa;
}

function sum(int accum, [Grade, int] grade) returns int {
    return accum + grade[1];
}

function gradeToValue([Grade, int] grade) returns [float, int] {
    match grade[0] {
        "A+" => {return [4.2, grade[1]];}
        "A" => {return [4.0, grade[1]];}
        "A-" => {return [3.7, grade[1]];}
        "B+" => {return [3.3, grade[1]];}
        "B" => {return [3.0, grade[1]];}
        "B-" => {return [2.7, grade[1]];}
        "C" => {return [2.0, grade[1]];}
        "F" => {return [0.0, grade[1]];}
    }
    error e = error("Invalid grade: " + <string>grade[0]);
    panic e;
}

function testIndexOf() returns [int?, int?] {
    [int, string, float, boolean, anydata] arr = [10, "foo", 12.34, true, <map<string>>{"k":"Bar"}];
    map<string> m = {"k":"Bar"};
    int? i1 = arr.indexOf(m);
    int? i2 = arr.indexOf(50);
    return [i1, i2];
}

function testPush1() returns int {
    [int, string, float...] tup = [10, "Ballerina"];
    tup.push(12.34, 45.67);
    return tup.length();
}

function testPush2() returns int {
    [int...] tup = [];
    tup.push(10, 11, 12, 13);
    return tup.length();
}

type Type [1, "hello"]|[2];

function testToStream() {
    Type tuple1 = [1, "hello"];
    stream<1|"hello"|2> stream1 = tuple1.toStream();
    test:assertValueEqual({value:1}, stream1.next());
    test:assertValueEqual({value:"hello"}, stream1.next());
    test:assertValueEqual((), stream1.next());
    stream<1|"hello"|2> stream2 = <stream<1|"hello"|2>> stream1;

    [int, string, boolean] tuple2 = [3, "hey", true];
    stream<int|string|boolean> stream3 = tuple2.toStream();
    stream<int|string|boolean> stream4 = <stream<int|string|boolean>> stream3;
    test:assertValueEqual({value:3}, stream4.next());
    test:assertValueEqual({value:"hey"}, stream4.next());
    test:assertValueEqual({value:true}, stream4.next());
    test:assertValueEqual((), stream4.next());
}

type castedStreamType stream<([int, string] & readonly)|([boolean, float] & readonly)>;

function testToStreamOnImmutableTuple() {
    [[int, string], [boolean, float]] & readonly immutableTuple = [[1, "two"], [true, 12.4]];
    var strm = immutableTuple.toStream();

    castedStreamType castedstrm = <castedStreamType> strm;
    test:assertValueEqual([1, "two"], castedstrm.next()?.value);
    test:assertValueEqual([true, 12.4], castedstrm.next()?.value);
    test:assertValueEqual((), castedstrm.next()?.value);
}

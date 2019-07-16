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

function testLength() returns int {
    int[] arr = [10, 20, 30, 40];
    return arr.length();
}

//function testIterator() returns string {
//    string[] arr = ["Hello", "World!", "From", "Ballerina"];
//    abstract object {
//         public function next() returns record {| string value; |}?;
//    } itr = arr.iterator();
//
//    record {| string value; |}|() elem = itr.next();
//    string result = "";
//
//    while (elem is record {| string value; |}) {
//        result += elem.value;
//        elem = itr.next();
//    }
//
//    return result;
//}

function testEnumerate() returns [int, string][] {
    string[] arr = ["Hello", "World!", "From", "Ballerina"];
    return arr.enumerate();
}

function testMap() returns int[] {
    int[] arr = [10, 20, 30, 40];
    int[] newArr = arr.'map(function (int x) returns int {
        return x/10;
    });
    return newArr;
}

function testForeach() returns string {
    string?[] arr = ["Hello", "World!", (), "from", "Ballerina"];
    string result = "";

    arr.forEach(function (string? x) {
        if (x is string) {
            result += x;
        }
    });

    return result;
}

function testSlice() returns float[] {
    float[] arr = [12.34, 23.45, 34.56, 45.67, 56.78];
    return arr.slice(1, 4);
}

function testRemove() returns [string, string[]] {
    string[] arr = ["Foo", "Bar", "FooFoo", "BarBar"];
    string elem = arr.remove(2);
    return [elem, arr];
}

function testSort() returns [int[], int[]] {
    int[] arr = [98, 34, 44, 87, 13, 2, 1, 13];
    int[] sorted = arr.sort(function (int x, int y) returns int {
        return x - y;
    });
    return [sorted, arr];
}

function testReduce() returns float {
    int[] arr = [12, 15, 7, 10, 25];
    float avg = arr.reduce(function (float accum, int val) returns float {
        return accum + <float>val / arr.length();
    }, 0.0);
    return avg;
}

type Grade "A+"|"A"|"A-"|"B+"|"B"|"B-"|"C"|"F";

function testIterableOpChain() returns float {
    [Grade, int][] grades = [["A+", 2], ["A-", 3], ["B", 3], ["C", 2]];

    int totalCredits = grades.reduce(function (int accum, [Grade, int] grade) returns int {
         return accum + grade[1];
     }, 0);

    float gpa = grades.'map(gradeToValue).reduce(function (float accum, [float, int] gradePoint) returns float {
        return accum + (gradePoint[0] * gradePoint[1]) / totalCredits;
    }, 0.0);

    return gpa;
}

function gradeToValue([Grade, int] grade) returns [float, int] {
    match grade[0] {
        "A+" => return [4.2, grade[1]];
        "A" => return [4.0, grade[1]];
        "A-" => return [3.7, grade[1]];
        "B+" => return [3.3, grade[1]];
        "B" => return [3.0, grade[1]];
        "B-" => return [2.7, grade[1]];
        "C" => return [2.0, grade[1]];
        "F" => return [0.0, grade[1]];
    }
    error e = error("Invalid grade: " + <string>grade[0]);
    panic e;
}

function testIndexOf() returns [int?, int?] {
    anydata[] arr = [10, "foo", 12.34, true, <map<string>>{"k":"Bar"}];
    map<string> m = {"k":"Bar"};
    int? i1 = arr.indexOf(m);
    int? i2 = arr.indexOf(50);
    return [i1, i2];
}

function testReverse() returns [int[], int[]] {
    int[] arr = [10, 20, 30, 40, 50];
    return [arr, arr.reverse()];
}

type Person record {|
    int id;
    string name;
    int pilotingScore;
    int shootingScore;
    boolean isForceUser;
|};

// example from: https://medium.com/poka-techblog/simplify-your-javascript-use-map-reduce-and-filter-bd02c593cc2d
function testIterableOpChain2() returns int {
    Person[] personnel = [
      {
        id: 5,
        name: "Luke Skywalker",
        pilotingScore: 98,
        shootingScore: 56,
        isForceUser: true
      },
      {
        id: 82,
        name: "Sabine Wren",
        pilotingScore: 73,
        shootingScore: 99,
        isForceUser: false
      },
      {
        id: 22,
        name: "Zeb Orellios",
        pilotingScore: 20,
        shootingScore: 59,
        isForceUser: false
      },
      {
        id: 15,
        name: "Ezra Bridger",
        pilotingScore: 43,
        shootingScore: 67,
        isForceUser: true
      },
      {
        id: 11,
        name: "Caleb Dume",
        pilotingScore: 71,
        shootingScore: 85,
        isForceUser: true
      }
    ];

    int totalJediScore = personnel.filter(function (Person p) returns boolean {
        return p.isForceUser;
    }).map(function (Person jedi) returns int {
        return jedi.pilotingScore + jedi.shootingScore;
    }).reduce(function (int accum, int val) returns int {
        return accum + val;
    }, 0);

    return totalJediScore;
}

function testForEach() returns string {
    string[] days = ["Sun", "Mon", "Tues"];
    string result = "";

    foreach var day in days {
        result += day;
    }

    return result;
}

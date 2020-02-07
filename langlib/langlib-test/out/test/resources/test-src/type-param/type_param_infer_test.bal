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


string words = "";
function concat(string value){
    words += value;
}

////////////// Test Array values.

function testArrayFunctionInfer(){
    string[] numbers = ["one", "two", "three"];

    // Test 1 - simple operations
    words = "";
    numbers.forEach( (ss) => concat(ss) );
    if(words != "onetwothree") {
        panic error("Test1", message = "word mismatched: " + words);
    }

    // Test 2 - chained operations.
    int[] test2 = numbers.map(ss => ss.toUpperAscii())
                          .filter( ss => ss == "ONE")
                          .map( ss => ss.length());
    if(test2.length() != 1 || test2[0] != 3) {
        panic error("Test2", message = "result mismatched", result=test2);
    }

    // Test 3 - Simple Reduce
    int test3 = numbers.map(s => s.length())
                     .reduce(<function (int, int) returns int>((a, b) => a + b), 0);
    if (test3 != 11) {
        panic error("Test3", message = "result mismatched", result=test3);
    }

    // Test 4 - Reduce with literals.
    int[] test4 = numbers.map(s => s.length())
                        .reduce(<function(int[], int) returns int[]>((a,b) => pushAndGet(a, b)), []);
    if (test4 != [3,3,5]) {
        panic error("Test4", message = "result mismatched", result=test4);
    }

    [string, int, boolean] someValue = [ "hello", 5, true];

    // Test 5 - Tuple test
    string[] test5 = someValue.map((a) => a.toString());
    if (test5 != ["hello","5","true"]) {
        panic error("Test5", message = "result mismatched", result=test5);
    }

    // Test 6 - chained functions.
    int[] test6 = someValue.map((a) => a.toString().length())
                           .reduce(<function(int[], int) returns int[]>((a,b) => pushAndGet(a, b)), []);
    if (test6 != [5,1,4]) {
       panic error("Test6", message = "result mismatched", result=test6);
    }
}

function pushAndGet(int[] ss, int value) returns int[] {
    ss.push(value);
    return ss;
}


////////////// Test Map values.

function testMapFunctionInfer(){
    map<string> numbers = {"1" : "one", "2" :"two", "3" :"three"};

    // Test 1.
    words = "";
    numbers.forEach( (ss) => concat(ss) );
    if(words != "onetwothree") {
        panic error("Test1", message = "word mismatched: " + words);
    }

     // Test 2 - chained operations.
    map<int> test2 = numbers.map(ss => ss.toUpperAscii())
                          .filter( ss => ss == "ONE")
                          .map( ss => ss.length());
    if(test2.length() != 1 || test2.get("1") != 3) {
        panic error("Test2", message = "result mismatched", result=test2);
    }

    // Test 3 - Simple Reduce
    int test3 = numbers.map(s => s.length())
                     .reduce(<function (int, int) returns int>((a, b) => a + b), 0);
    if (test3 != 11) {
        panic error("Test3", message = "result mismatched", result=test3);
    }

    // Test 4 - Reduce with literals.
    int[] test4 = numbers.map(s => s.length())
                        .reduce(<function(int[], int) returns int[]>((a,b) => pushAndGet(a, b)), []);
    if (test4 != [3,3,5]) {
        panic error("Test4", message = "result mismatched", result=test4);
    }

   record {
    string s;
    int i;
    boolean b;
   } someValue = {s: "hello", i:  5, b: true};

    // Test 5 - Record test
    map<string> test5 = someValue.map((a) => a.toString());
    if (test5.get("s") != "hello" && test5.get("i") != "5" && test5.get("b") != "true") {
        panic error("Test5", message = "result mismatched", result=test5);
    }

    // Test 6 - chained functions.
    int[] test6 = someValue.map((a) => a.toString().length())
                           .reduce(<function(int[], int) returns int[]>((a,b) => pushAndGet(a, b)), []);
    if (test6 != [5,1,4]) {
       panic error("Test6", message = "result mismatched", result=test6);
    }
}

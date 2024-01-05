// Copyright (c) 2023 WSO2 LLC.
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

import ballerina/test;
import ballerina/lang.runtime;

function workerMultipleReceiveTest1() {
    worker w1 {
        2 -> w2;
    }

    worker w2 returns map<int> {
        map<int> result = <- { w1, w3};
        return result;
    }

    worker w3 {
        runtime:sleep(2);
        3 -> w2;
    }

    map<int> results = wait w2;
    test:assertEquals(results["w1"], 2, "Invalid int result");
    test:assertEquals(results["w3"], 3, "Invalid int result");
}

function workerMultipleReceiveTest2() {
    worker w1 {
        2 -> w2;
    }

    worker w2 returns map<int> {
        map<int> result = <- { a:w1, b:w3};
        return result;
    }

    worker w3 {
        runtime:sleep(2);
        3 -> w2;
    }

    map<int> results = wait w2;
    test:assertEquals(results["a"], 2, "Invalid int result");
    test:assertEquals(results["b"], 3, "Invalid int result");
}

function workerMultipleReceiveTest3() {
    worker w1 {
        100 -> w2;
        20 -> w2;
    }

    worker w2 returns map<anydata> {
        map<anydata> result = {} ;
        map<int> m = <- { a:w1, b:w3 }; 
        result["first"] = m;
        m = <- { aa:w1, bb:w3 }; 
        result["second"] = m;
        return result;
    }

    worker w3 {
        6 -> w2;
        45 -> w2;
    }

    map<anydata> mapResult = wait w2;
    test:assertEquals(mapResult["first"], { "a":100, "b":6}, "Invalid map result");
    test:assertEquals(mapResult["second"], { "aa":20, "bb":45}, "Invalid map result");
}

type MyRec record {|
    int firstValue;
    int secondValue;
|};

function workerMultipleReceiveWithUserDefinedRecord() {
    worker w1 {
        100 -> w2;
        20 -> w2;
    }

    worker w2 returns map<anydata> {
        map<anydata> result = {} ;
        MyRec m = <- { firstValue:w1, secondValue:w3 }; 
        result["first"] = m;
        m = <- { firstValue:w1, secondValue:w3 }; 
        result["second"] = m;
        return result;
    }

    worker w3 {
        6 -> w2;
        45 -> w2;
    }

    map<anydata> mapResult = wait w2;
    test:assertEquals(mapResult["first"], {"firstValue":100,"secondValue":6}, "Invalid map result");
    test:assertEquals(mapResult["second"], {"firstValue":20,"secondValue":45}, "Invalid map result");
}
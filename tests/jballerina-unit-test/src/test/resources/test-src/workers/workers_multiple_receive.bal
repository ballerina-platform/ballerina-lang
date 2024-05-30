// Copyright (c) 2024 WSO2 Inc. (http://www.wso2.com).
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
import ballerina/lang.'error as errorLib;

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

function workerMultipleReceiveWithErrorReturn() {
    worker w1 returns error?{
        int v = 10;
        if v == 10 {
            return error("Error in worker w1");
        }
        100 -> w2;
    }

    worker w2 returns map<int|error> {
        map<int|error> m = <- { firstValue:w1, secondValue:w3 }; 
        return m;
    }

    worker w3 {
        6 -> w2;
    }

    map<int|error> mapResult = wait w2;
    test:assertTrue(mapResult["firstValue"] is error, "Invalid map result");
    error e = <error> mapResult["firstValue"];
    test:assertEquals(e.message(), "Error in worker w1", "Invalid error message");
    test:assertEquals(mapResult["secondValue"], 6, "Invalid int result");
}

function workerMultipleReceiveWithAllErrorReturn() {
    worker w1 returns error?{
        int v = 10;
        if v == 10 {
            return error("Error in worker w1");
        }
        100 -> w2;
    }

    worker w2 returns map<int|error> {
        map<int|error> m = <- { firstValue:w1, secondValue:w3 }; 
        return m;
    }

    worker w3 returns error?{ 
        int v = 10;
        if v == 10 {
            return error("Error in worker w3");
        }
        6 -> w2;
    }

    map<int|error> mapResult = wait w2;
    test:assertTrue(mapResult["firstValue"] is error, "Invalid map result");
    error e = <error> mapResult["firstValue"];
    test:assertEquals(e.message(), "Error in worker w1", "Invalid error message");
    test:assertTrue(mapResult["secondValue"] is error, "Invalid map result");
    e = <error> mapResult["secondValue"];
    test:assertEquals(e.message(), "Error in worker w3", "Invalid error message");
}

function workerMultipleReceiveWithPanic() {
    worker w1 {
        int v = 10;
        if v == 10 {
            panic error("Error in worker w1");
        }
        100 -> w2;
    }

    worker w2 returns map<int> {
        map<int> m = <- { firstValue:w1, secondValue:w3 }; 
        return m;
    }

    worker w3 {
        6 -> w2;
    }

    map<int>|error result = trap wait w2;
    test:assertTrue(result is error, "Invalid map result");
    error e = <error> result;
    test:assertEquals(e.message(), "Error in worker w1", "Invalid error message");
}

function workerMultipleReceiveWithAllPanic() {
    worker w1 {
        runtime:sleep(2);
        int v = 10;
        if v == 10 {
            panic error("Error in worker w1");
        }
        100 -> w2;
    }

    worker w2 returns map<int> {
        map<int> m = <- { firstValue:w1, secondValue:w3 }; 
        return m;
    }

    worker w3 {
        int v = 10;
        if v == 10 {
            panic error("Error in worker w3");
        }
        6 -> w2;
    }

    map<int>|error result = trap wait w2;
    test:assertTrue(result is error, "Invalid map result");
    error e = <error> result;
    test:assertEquals(e.message(), "Error in worker w3", "Invalid error message");
}

function workerMultipleReceiveWithErrorReturnRec() {
    worker w1 returns error?{
        int v = 10;
        if v == 10 {
            return error("Error in worker w1");
        }
        100 -> w2;
    }

    worker w2 returns ErrRec {
        ErrRec m = <- { firstValue:w1, secondValue:w3 }; 
        return m;
    }

    worker w3 {
        6 -> w2;
    }

    ErrRec mapResult = wait w2;
    test:assertTrue(mapResult.firstValue is error, "Invalid map result");
    error e = <error> mapResult.firstValue;
    test:assertEquals(e.message(), "Error in worker w1", "Invalid error message");
    test:assertEquals(mapResult.secondValue, 6, "Invalid int result");
}

type ErrRec record {|
    int|error firstValue;
    int secondValue;
|};

function workerMultipleReceiveWithConditionalSend1() {
    boolean foo = true;

    worker w1 {
        if foo {
            1 -> w2;
        } else {
            2 -> w3;
        }
    }

    worker w2 returns map<int|errorLib:NoMessage> {
        map<int|errorLib:NoMessage> m = <- {w1, w3};
        return m;
    }

    worker w3 {
        int|errorLib:NoMessage a = <- w1;
        test:assertTrue(a is error, "Invalid result");
        error e = <error> a;
        test:assertEquals(e.message(), "NoMessage", "Invalid error message");
        test:assertEquals(e.detail().toString(), "{\"message\":\"no message received from worker 'w1' to worker 'w3'\"}", "Invalid error detail");
        3 -> w2;
    }

    map<int|errorLib:NoMessage> mapResult = wait w2;
    test:assertEquals(mapResult["w1"], 1, "Invalid map result");
    test:assertEquals(mapResult["w3"], 3, "Invalid map result");
}

function workerMultipleReceiveWithConditionalSend2() {
    boolean foo = true;

    worker w1 {
        if foo {
            1 -> w3;
        } else {
            2 -> w2;
        }
    }

    worker w2 returns map<int|errorLib:NoMessage> {
        map<int|errorLib:NoMessage> m = <- {w1, w3};
        return m;
    }

    worker w3 {
        int|errorLib:NoMessage a = <- w1;
        test:assertTrue(a is int, "Invalid result");
        test:assertEquals(a, 1, "Invalid int result");
        3 -> w2;
    }

    map<int|errorLib:NoMessage> mapResult = wait w2;
    test:assertTrue(mapResult["w1"] is errorLib:NoMessage, "Invalid map result");
    errorLib:NoMessage e = <errorLib:NoMessage> mapResult["w1"];
    test:assertEquals(e.message(), "NoMessage", "Invalid error message");
    test:assertEquals(e.detail().toString(), "{\"message\":\"no message received from worker 'w1' to worker 'w2'\"}", "Invalid error detail");
    test:assertEquals(mapResult["w3"], 3, "Invalid map result");
}

function workerMultipleReceiveWithConditionalSend3() {
    worker w1 {
      boolean b1 = true;
      boolean b2 = true;
      boolean b3 = false;

      if b1 {
         1 -> function;
      } else if b2 {
         2 -> function;
      } else if b3 {
         3 -> function;
      } else {
         4 -> function;
      }
    }

    map<int|errorLib:NoMessage> mapResult = <- {a:w1, b:w1, c:w1, d:w1};
    test:assertEquals(mapResult["a"], 1);

    int|errorLib:NoMessage? bResult = mapResult["b"];
    test:assertTrue(bResult is errorLib:NoMessage);
    errorLib:NoMessage e = <errorLib:NoMessage> bResult;
    test:assertEquals(e.message(), "NoMessage", "Invalid error message");
    test:assertEquals(e.detail().toString(), "{\"message\":\"no message received from worker 'w1' to worker 'function'\"}", "Invalid error detail");

    test:assertTrue(mapResult["c"] is errorLib:NoMessage);
    test:assertTrue(mapResult["d"] is errorLib:NoMessage);
}

// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
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
import ballerina/lang.'error as errorLib;

function workerConditionalSendTest() {
    boolean foo = true;

    worker w1 {
        if foo {
            1 -> w2;
        } else {
            2 -> w3;
        }
    }

    worker w2 returns int|errorLib:NoMessage {
        int|errorLib:NoMessage a = <- w1;
        return a;
    }

    worker w3 {
        int|errorLib:NoMessage b = <- w1;
        int|errorLib:NoMessage c = <- w4;
    }

    worker w4 {
        if !foo {
            1 -> w3;
        } else {
            2 -> w5;
        }
    }
    
    worker w5 returns int|errorLib:NoMessage {
        int|errorLib:NoMessage d = <- w4;
        return d;
    }

    map<int|errorLib:NoMessage> results = wait {a: w2, b: w5};
    test:assertEquals(results["a"], 1, "Invalid int result");
    test:assertEquals(results["b"], 2, "Invalid int result");
}

function sameWorkerSendTest() {
    boolean foo = true;

    worker w1 {
        if foo {
            10 -> w2;
        } else {
            20 -> w2;
        }
    }

    worker w2 {
        int|errorLib:NoMessage a = <- w1;
        int|errorLib:NoMessage b = <- w1;
        test:assertEquals(a, 10, "Invalid int result");
        test:assertTrue(b is error);
        error e = <error> b;
        test:assertEquals(e.message(), "NoMessage", "Invalid error message");
        test:assertEquals(e.detail().toString(), "{\"message\":\"no message received from worker 'w1' to worker 'w2'\"}", "Invalid error detail");
    }

    _ = wait {a: w1, b: w2};
}

function sameWorkerSendEitherOnePath() {
    boolean foo = true;

    worker w1 {
        if foo {
            10 -> w2;
        } else {
            20 -> w2;
        }
    }

    worker w2 {
        int|errorLib:NoMessage a = <- w1 | w1;
        test:assertEquals(a, 10, "Invalid int result");
    }

    _ = wait {a: w1, b: w2};
}

function sameWorkerSendAltReceiveSendError() {
    boolean foo = true;

    worker w1 returns error? {
        if foo {
            if foo && true {
                return error("Error in worker 1");
            } else {
                10 -> w2;
            }
        } else {
            20 -> w2;
        }
    }

    worker w2 {
        int|error a = <- w1 | w1;
        test:assertTrue(a is error, "Invalid error result");
        error e = <error>a;
        test:assertEquals(e.message(), "NoMessage", "Invalid error message");
        test:assertEquals(e.detail().toString(), "{\"message\":\"no worker message received for channel 'w1->w2'\"}", "Invalid error detail");
    }
    error? unionResult = wait w1;
    test:assertTrue(unionResult is error, "Invalid error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
}

function sameWorkerSendAltReceiveReceiverError() {
    boolean foo = true;

    worker w1 returns error? {
        if foo {
            if foo && true {
                return error("Error in worker 1");
            } else {
                10 -> w2;
            }
        } else {
            20 -> w2;
        }
    }

    worker w2 returns int|error {
        int value = 10;
        if value == 10 {
            return error("Error in worker 2");
        }
        int|error a = <- w1 | w1;
        return a;
    }

    int|error unionResult = wait w2;
    test:assertTrue(unionResult is error, "Expected error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 2", "Invalid error message");
}

function sameWorkerSendElse() {
    boolean foo = false;

    worker w1 returns error? {
        if foo {
            if foo && true {
                return error("Error in worker 1");
            } else {
                10 -> w2;
            }
        } else {
            20 -> w2;
        }
    }

    worker w2 {
        int|error a = <- w1 | w1;
        test:assertTrue(a is int, "Invalid int result");
        test:assertEquals(a, 20, "Wrong int value received");
    }

    error? unionResult = wait w1;
    test:assertTrue(unionResult is (), "Invalid result");
}

function sameWorkerSendSenderPanic() {

    boolean foo = true;

    worker w1 {
        if foo {
            if foo && true {
                panic error("Error in worker 1");
            } else {
                10 -> w2;
            }
        } else {
            20 -> w2;
        }
    }

    worker w2 {
        int|errorLib:NoMessage a = <- w1 | w1;
    }

    error? unionResult = trap wait w1;
    test:assertTrue(unionResult is error, "Invalid error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
}

function sameWorkerSendReceiverPanic() {
    boolean foo = true;

    worker w1 {
        if foo {
            10 -> w2;
        } else {
            20 -> w2;
        }
    }

    worker w2 returns int|errorLib:NoMessage {
        int value = 10;
        if value == 10 {
            panic error("Error in worker 2");
        }
        int|errorLib:NoMessage a = <- w1 | w1;
        return a;
    }

    int|error unionResult = trap wait w2;
    test:assertTrue(unionResult is error, "Expected error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 2", "Invalid error message");
}

function sameWorkerSendMultiplePath1() {
    boolean foo = true;

    worker w1 {
        if foo {
            1 -> w2;
        } 
        2 -> w2;  
    }

    worker w2 returns int|errorLib:NoMessage {
        int|errorLib:NoMessage a = <- w1 | w1;
        return a;
    }

    int|errorLib:NoMessage intResult = wait w2;
    test:assertEquals(intResult, 1, "Invalid int result");
}

function sameWorkerSendMultiplePath2() {
    boolean foo = false;

    worker w1 {
        if foo {
            1 -> w2;
        } 
        2 -> w2;  
    }

    worker w2 returns int|errorLib:NoMessage {
        int|errorLib:NoMessage a = <- w1 | w1;
        return a;
    }

    int|errorLib:NoMessage intResult = wait w2;
    test:assertEquals(intResult, 2, "Invalid int result");
}

function sameWorkerSendMultiplePathError1() {
    boolean foo = false;

    worker w1 returns error? {
        int value = 10;
        if foo {
            if value == 10 {
                return error("Error in worker 1");
            }
            1 -> w2;
        } 
        2 -> w2;  
    }

    worker w2 returns int|error? {
        int|error a = <- w1 | w1;
        return a;
    }

    map<error|int?> mapResult = wait { a : w1, b: w2};
    test:assertTrue(mapResult["a"] is (), "Invalid nil result");
    test:assertEquals(mapResult["b"], 2, "Invalid int result");
}

function sameWorkerSendMultiplePathError2() {
    boolean foo = false;

    worker w1 returns error? {
        int value = 10;
        if foo {
            1 -> w2;
        } 
        if value == 10 {
            return error("Error in worker 1");
        }
        2 -> w2;  
    }

    worker w2 returns int|error {
        int|error a = <- w1 | w1;
        return a;
    }

    map<error|int?> mapResult = wait { a : w1, b: w2};
    test:assertTrue(mapResult["a"] is error, "Invalid error result");
    test:assertTrue(mapResult["b"] is error, "Invalid error result");
    error e = <error> mapResult["a"];
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
    e = <error> mapResult["b"];
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
}

function sameWorkerSendMultiplePathError3() {
    boolean foo = true;

    worker w1 returns error? {
        int value = 10;
        if foo {
            if value == 10 {
                return error("Error in worker 1");
            }
            1 -> w2;
        } 
        2 -> w2;  
    }

    worker w2 returns int|error? {
        int|error a = <- w1 | w1;
        return a;
    }
    
    map<error|int?> mapResult = wait { a : w1, b: w2};
    test:assertTrue(mapResult["a"] is error, "Invalid error result");
    test:assertTrue(mapResult["b"] is error, "Invalid error result");
    error e = <error> mapResult["a"];
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
    e = <error> mapResult["b"];
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
}

function sameWorkerSendMultiplePathError4() {
    boolean foo = true;

    worker w1 returns error? {
        int value = 10;
        if foo {
            1 -> w2;
        } 
        if value == 10 {
            return error("Error in worker 1");
        }
        2 -> w2;  
    }

    worker w2 returns int|error {
        int|error a = <- w1 | w1;
        return a;
    }

    map<error|int?> mapResult = wait { a : w1, b: w2};
    error e = <error> mapResult["a"];
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
    test:assertEquals(mapResult["b"], 1, "Invalid int result");
}

function multipleReceiveConditional() {
    boolean foo = true;
    worker w1 returns int {
        if (foo) {
            int x = 2;
            x -> w2;
            return x;
        } else {
            2-> w3;
        }
        int y =  <- w3;
        return y;
    }
    
    worker w2 returns int|errorLib:NoMessage {
        int|errorLib:NoMessage y = <- w1;
        return y;
    }

    worker w3 returns int|errorLib:NoMessage {
        int|errorLib:NoMessage y = <- w1;
        3 -> w1;
        return y;
    }

    map<int|error?> mapResult = wait {a: w1, b: w2, c: w3};
    test:assertEquals(mapResult["a"], 2, "Invalid int result");
    test:assertEquals(mapResult["b"], 2, "Invalid int result");
    test:assertTrue(mapResult["c"] is error, "Expected error result");
    error e = <error>mapResult["c"];
    test:assertEquals(e.message(), "NoMessage", "Invalid error message");
    test:assertEquals(e.detail().toString(), "{\"message\":\"no message received from worker 'w1' to worker 'w3'\"}", "Invalid error detail");
}

function multipleReceiveWithNonConditionalSend() {
    boolean foo = true;
    worker w3 {
        int|errorLib:NoMessage x = <- w1;
        int|errorLib:NoMessage y = <- w1;
        int z = <- w1;
        test:assertEquals(x, 2, "Invalid int result");
        test:assertTrue(y is error, "Invalid error result");
        error e = <error>y;
        test:assertEquals(e.message(), "NoMessage", "Invalid error message");
        test:assertEquals(e.detail().toString(), "{\"message\":\"no message received from worker 'w1' to worker 'w3'\"}", "Invalid error detail");
        test:assertEquals(z, 4, "Invalid int result");
     }

    worker w1 {
        if foo {
            2 -> w3;
        } else {
            3 -> w3;
        }
        4 -> w3;
   }
   wait w3;
}

function testNonTopLevelSend() {
  worker w1 returns boolean {
      int i = 2;
      if (0 > 1) {
           return true;
      }
      i -> w2;
      return false;
    }

    worker w2 returns int|errorLib:NoMessage {
      int|errorLib:NoMessage j = 25;
      j = <- w1;
      return j;
    }

    map<boolean|int|errorLib:NoMessage> mapResult = wait {a: w1, b: w2};
    test:assertEquals(mapResult["a"], false, "Invalid boolean result");
    test:assertEquals(mapResult["b"], 2, "Invalid int result");
}

function testSendWithEarlyReturnError() {
  worker w1 returns boolean|error {
      int i = 2;
      if (0 > 1) {
           return error("Error in worker 1");
      }
      i -> w2;
      return false;
    }

    worker w2 returns int|error {
      int|error j = 25;
      j = <- w1;
      return j;
    }

    map<boolean|int|error> mapResult = wait {a: w1, b: w2};
    test:assertEquals(mapResult["a"], false, "Invalid boolean result");
    test:assertEquals(mapResult["b"], 2, "Invalid int result");
}

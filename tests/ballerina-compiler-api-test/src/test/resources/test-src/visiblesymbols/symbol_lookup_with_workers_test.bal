// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

string aString = "foo";

public function workerSendToWorker() returns int {
    @strand{thread:"any"}
    worker w1 {
      int i = 40;
      i -> w2;

      float x = 12.34;
      x ->> w2;

      var res = flush w2;
    }

    @strand{thread:"any"}
    worker w2 returns int {
      int j = 25;
      j = <- w1;

      float y = <- w1;
      return j;
    }
    int ret = wait w2;

    return ret + 1;
}

function testFork() {
    int x = 10;

    fork {
        worker w1 {
          int i = x;
          // cursor pos
        }
    }
}

function testWorkerSendReceive() {
    worker wrk1 {
        int a = 6;
        a ->
    }

    worker wrk2 {
    }

    worker wrk3 {
        int b = <-
    }
}

function testFlushWithoutWorkerName() {
    worker w1 {
        10 -> w2;
        error? err = flush ;
    }

    worker w2 {
        int receivedVal = <- w1;
    }
}

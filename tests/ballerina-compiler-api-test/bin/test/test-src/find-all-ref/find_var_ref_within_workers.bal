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

function basicTest() returns int {
    int a = 10;

    worker w1 {
       a += 10;
    }

    worker w2 returns int {
       int i = 0;
       while (i < 100) {
          i +=1;
       }
       return i;
    }

    int result = wait w2;
    wait w1;

    return a + result;
}

function workerSendTest() {
    worker w1 {
      int i = 40;
      i -> w2;
    }

    worker w2 returns int {
      int i = 25;
      i = <- w1;
      return i;
    }

    map<int> ret = wait {w1, w2};
}

function workerSyncSendAndFlushTest() {
    worker w1 {
      int i = 40;
      i ->> w2;
      error? err = flush w2;
    }

    worker w2 returns int {
      int i = 25;
      i = <- w1;
      return i;
    }
}

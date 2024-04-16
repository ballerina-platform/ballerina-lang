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

function invalidWorkerReceiveBeforeWorker() {
  int _ = <- w1;
  worker w1 {
    int i = 1;
    i -> function;
  }
}

function workerReceiveAfterNonErrorReturn1() {
    worker w1 {
        1 ->> w2;
        2 ->> w2;
    }

    worker w2 {
        boolean b = true;

        int _ = <- w1;

        if b {
            return;
        }

        int _ = <- w1;
    }

    wait w1;
}

function workerReceiveAfterNonErrorReturn2() {
    worker w1 {
        1 ->> w2;
        2 ->> w2;
    }

    worker w2 {
        boolean b = true;

        if b {
            return;
        }

        int _ = <- w1|w1;
    }

    wait w1;
}

// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function workerMessagePassingAfterWait() {
    worker w {
        5 -> function;
    }
    wait w;
    int _ = <- w;
}

function workerWaitsForWorker() {
    worker w {
        int j = 0;
        j -> w1;
    }

    worker w1 {
        wait w;
        int _ = <- w;
    }
}


public function forkTest() {
    fork {
        worker w {
            1 -> w2;
        }
        worker w1 {
            2 -> w2;
        }
    }
    worker w2 {
        wait w;
        wait w1;
        int i = <- w;
        i = <- w1;
    }

    worker wi {
        fork {
            worker wii {
                2 -> wij;
            }
            worker wij {
                wait wii;
                int _ = <- wii;
            }
        }
    }
}

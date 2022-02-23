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
//

function waiter1() {
    worker w {
        wait v;
    }

    worker v {
        wait w;
    }
}

function waiter2() {
    worker w {
        wait v;
    }

    worker x {
        wait w;
    }

    worker v {
        wait x;
    }
}

function workerWaitsForWorker() {
    worker w {
        int j = 0;
        wait w2;
        j -> w1;
    }

    worker w1 {
        int _ = <- w;
    }

    worker w2 {
        wait w1;
    }
}


public function forkTest() {
    fork {
        worker w {
            wait v;
        }

        worker x {
            wait w;
        }

        worker v {
            wait x;
        }
    }
}

public function testForkWithinWorker() {
    worker wi {
        fork {
            worker wi {
                wait vi;
            }

            worker xi {
                wait wi;
            }

            worker vi {
                wait xi;
            }
        }
    }
}

public function singleUnfinishedWorker() {
    worker w1 {
        11 -> w3;
        string _ = <- w3;
    }
    worker w3 {
        "tow1-1" -> w2;
        int _ = <- w2;
    }
    worker w2 {
        string _ = <- w3;
        24 -> w3;
    }
    _ = wait {w1, w2, w3};
}

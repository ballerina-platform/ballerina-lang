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

function case1() {
    worker w1 {
        20 -> w2;
    }

    worker w2 {
        "xxx" -> w1;
    }
}

function case2() {
    worker w1 {
        int _ = <- w2;
    }

    worker w2 {
        string _ = <- w1;
    }
}

function case3() {
    boolean foo = true;
    worker w1 {
        if foo {
            2 -> w2;
        } else {
            3 -> w2;
        }
        4 -> w2;
    }

    worker w2 returns error? {
        _ = check <- w1;
        _ = check <- w1;
    }
}

function case4() {
    worker w1 {
        "xxx" -> w2;
    }

    worker w2 {
        _ = <- w1 | w1;
    }
}

function case5() {
    worker w1 {
        "xxx" -> w2;
    }

    worker w2 {
        _ = <- {a: w1, b: w3, c: w3};
    }

    worker w3 {
        "yyy" -> w2;
    }
}

function case6() {
    worker w1 {
    }

    worker w2 {
        _ = <- {a: w1, b: w3, c: w3};
    }

    worker w3 {
        "yyy" -> w2;
    }
}

function case7() {
    fork {
        worker w1 {
            int a = 5;
            int b = 0;
            a -> w2;
            b = <- w3;
        }
        worker w2 {
            int a = 0;
            int b = 15;
            a = <- w1;
            a -> w3;
        }
        worker w3 {
            int a = 0;
            int b = 15;
            a = <- w2;
        }
    }
}

// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type firstRec record {
    int id = 1;
    string name = "default";
};

type secondRec record {
    int f1 = 1;
    string f2 = "default";
};

type thirdRec record {
    int f1;
    string f2;
    int f4?;
};

type Person record {
    int age = 1;
    string name = "default";
};

function workerActionFirstTest() {
    worker w1 {
        Person p1 = {};
        // Async send expr should be of anydata
        p1 -> w2;
        // Sync send expr should be of anydata
        var result = p1 ->> w2;
        any|error val = result;
        // Invalid worker
        // var x = flush w4;
    }
    worker w2 returns error? {
        // Receive expr should get anydata
        if(0 > 1){
             error err = error("err", message = "err msg");
             return err;
        }
        Person _ = <- w1;
        Person p3 = {};
        p3 = <- w1;
        return;
    }
    worker w3 {
        // No send actions to particular worker
        var x = flush w1;
        any|error val = x;
    }
}

function workerActionSecTest() {
    worker w1 {
        int i =10;
        i -> w2;

        string msg = "hello";
        msg -> w2;
    }

    worker w2 {
        print("w1");
        string msg = "default";
        if (true) {
            msg = <- w1;
        }
    }
}

function workerActionThirdTest() {
    worker w1 {
        int i = 5;
        error? x1 = i ->> w2;
        error? x2 = i ->> w2;
        error? result = flush w2;
    }
    worker w2 returns error?{
        if(0 > 1){
             error err = error("err", message = "err msg");
             return err;
        }
        int j =0 ;
        j = <- w1;
        j = <- w1;
        return;
    }
}

function invalidReceiveUsage() {
    fork {
        worker w1 {
            int a = 5;
            a -> w2;
        }
        worker w2 {
            int _ = <- w1;
        }
    }
}

function print(string str) {
    string _ = str.toUpperAscii();
}

function getId() returns int {
    return 10;
}

function getName() returns string {
    return "Natasha";
}

function getStatus() returns boolean {
    return true;
}

function getStdId() returns future<int> {
    future <int> id = start getId();
    return id;
}

public function workerAsAFutureTest() returns int {
    worker wx returns int {
        any _ = <- wy;
        "h" -> wy;
        future<int> fi = wy; // illegal peer worker ref
        return checkpanic wait fi;
    }

    worker wy returns int {
        "a" -> wx;
        string _ = <- wx;

        fork {
            worker wix returns int {
                int _ = <- wiy;
                var _ = wiy; // illegal peer worker ref within a worker

                return 0;
            }
            worker wiy {
                0 -> wix;
                _ = wait wix; // illegal peer worker ref within a worker
                _ = wait wx; // illegal peer worker ref within a worker
            }
        }

        future<int> _ = wix;
        int _ = wait wix;
        future<int> _ = wx; // illegal peer worker ref within a worker
        return wait wx; // illegal peer worker ref within a worker
    }

    function () returns int lambda0 = function () returns int {
        worker lw0 {
            _ = wait lw1; // illegal peer worker ref within a worker
        }

        worker lw1 {

        }
        return 1+2;
    };

    future<int> fLambda0 = start lambda0();

    function () returns int|error lambda1 = function () returns int|error {
        return wait fLambda0;
    };
    future<int|error> _ = start lambda1();

    return wait wy;
}

class ObjFuncUsingWorkersAsFutureValues {
    function foo() returns int {
        worker wx returns int {
            any _ = <- wy;
            "h" -> wy;
            future<int> fi = wy; // illegal peer worker ref
            var _ = function () {
                _ = wait wy; // illegal peer worker ref within a worker
            };
            return checkpanic wait fi;
        }

        worker wy returns int {
            "a" -> wx;
            string _ = <- wx;

            fork {
                worker wix returns int {
                    int _ = <- wiy;
                    var _ = wiy; // illegal peer worker ref within a worker

                    return 0;
                }
                worker wiy {
                    0 -> wix;
                    _ = wait wix; // illegal peer worker ref within a worker
                    _ = wait wx; // illegal peer worker ref within a worker
                    function (future<int>) returns future<int> f = (a) => wx; // illegal peer worker ref within a worker
                    future<int> p = start bar();
                    future<int> _ = f(p);
                }
            }

            future<int> _ = wix;
            int _ = wait wix;
            future<int> _ = wx; // illegal peer worker ref within a worker
            return wait wx; // illegal peer worker ref within a worker
        }
        return wait wy;
    }

}

function bar() returns int {
    return  1;
}


function testUnsupportedWorkerPosition() {
    worker w {
        if 1 / 2 == 0 {
            0 -> function;
            if true {
                1 -> function;
            }
        }

        int i = 6;
        foreach var index in 0 ..< i {
            index -> function;
            index -> w1;
        }

        while (true) {
            i -> function;
            i -> w1;
        }

        match i {
            1 => {
                i -> function;
            }
            2 => {
                i -> w1;
            }
        }

        function _ = function() {
                         i -> w1;
                     };
    }

    worker w1 {
        int i = 6;
        foreach var index in 0 ..< i {
            int _ = <- w;
        }
    }

    if (2 / 2 == 1) {
        int _ = <- w;
    }
}

function f = function() {
                 worker w {
                     if 1 / 2 == 0 {
                         0 -> function;
                         if true {
                             1 -> function;
                         }
                     }

                     int i = 6;
                     foreach var index in 0 ..< i {
                         index -> function;
                         index -> w1;
                     }

                     while (true) {
                         i -> function;
                         i -> w1;
                     }

                     match i {
                         1 => {
                             i -> function;
                         }
                         2 => {
                             i -> w1;
                         }
                     }

                     function _ = function() {
                                      i -> w1;
                                  };
                 }

                 worker w1 {
                     int i = 6;
                     foreach var index in 0 ..< i {
                         int _ = <- w;
                     }
                 }

                 if (2 / 2 == 1) {
                     int _ = <- w;
                 }
             };

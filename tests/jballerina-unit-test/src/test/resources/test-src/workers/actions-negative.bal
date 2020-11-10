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
        // Invalid worker
        // var x = flush w4;
    }
    worker w2 returns error? {
        // Receive expr should get anydata
        if(false){
             error err = error("err", message = "err msg");
             return err;
        }
        Person p2 = <- w1;
        Person p3 = {};
        p3 = <- w1;
        return;
    }
    worker w3 {
        // No send actions to particular worker
        var x = flush w1;
    }
}

function workerActionSecTest() {
    worker w1 {
        int i =10;
        i -> w2;

        string msg = "hello";
        msg -> w2;

        if (true) {
            i -> w2;
        }
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
        var x1 = i ->> w2;
        var x2 = i ->> w2;
        var result = flush w2;
    }
    worker w2 returns error?{
        if(false){
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
            int a = <- w1;
        }
    }
}

function print(string str) {
    string result = str.toUpperAscii();
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
        any a = <- wy;
        "h" -> wy;
        future<int> fi = wy; // illegal peer worker ref
        return wait fi;
    }

    worker wy returns int {
        "a" -> wx;
        string k = <- wx;

        fork {
            worker wix returns int {
                int ji = <- wiy;
                var fwiy = wiy; // illegal peer worker ref within a worker

                return 0;
            }
            worker wiy {
                0 -> wix;
                _ = wait wix; // illegal peer worker ref within a worker
                _ = wait wx; // illegal peer worker ref within a worker
            }
        }

        future<int>  wixF = wix;
        int wixK = wait wix;
        future<int> fn = wx; // illegal peer worker ref within a worker
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

    function () returns int lambda1 = function () returns int {
        return wait fLambda0;
    };
    future<int> fLambda1 = start lambda1();

    return wait wy;
}

class ObjFuncUsingWorkersAsFutureValues {
    function foo() returns int {
        worker wx returns int {
            any a = <- wy;
            "h" -> wy;
            future<int> fi = wy; // illegal peer worker ref
            var f = function () {
                _ = wait wy; // illegal peer worker ref within a worker
            };
            return wait fi;
        }

        worker wy returns int {
            "a" -> wx;
            string k = <- wx;

            fork {
                worker wix returns int {
                    int ji = <- wiy;
                    var fwiy = wiy; // illegal peer worker ref within a worker

                    return 0;
                }
                worker wiy {
                    0 -> wix;
                    _ = wait wix; // illegal peer worker ref within a worker
                    _ = wait wx; // illegal peer worker ref within a worker
                    function (future<int>) returns future<int> f = (a) => wx; // illegal peer worker ref within a worker
                    future<int> p = start bar();
                    future<int> wxRef = f(p);
                }
            }

            future<int>  wixF = wix;
            int wixK = wait wix;
            future<int> fn = wx; // illegal peer worker ref within a worker
            return wait wx; // illegal peer worker ref within a worker
        }
        return wait wy;
    }

}



function bar() returns int {
    return  1;
}

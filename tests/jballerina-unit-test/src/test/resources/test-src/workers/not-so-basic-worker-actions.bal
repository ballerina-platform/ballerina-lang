import ballerina/runtime;

function forkWithTimeoutTest1() returns map<anydata> {
    map<any> m = {};
    fork {
        worker w1 {
            int a = 5;
            int b = 0;
            a -> w2;
            b = <- w2;
        }
        worker w2 {
            int a = 0;
            int b = 15;
            a = <- w1;
            b -> w1;
            runtime:sleep(5000);
        }
    }
    worker w3 returns map<any> {
        map<any> results = wait {w1, w2};
        m["x"] = 25;
        return m;
    }

    future<map<any>> f = start timeoutFunction1(1000, m);
    map<anydata> waitedResult = (wait w3 | f);
    var result = waitedResult.clone();
    return result;
}

function forkWithTimeoutTest2() returns map<anydata> {
    map<any> m = {};
    fork {
        worker w1 {
            int a = 5;
            int b = 0;
        }
        worker w2 {
            int a = 0;
            int b = 15;
        }
    }
    map<any> results = wait {w1, w2};
    worker w3 returns map<any> {
        runtime:sleep(1000);
        m["x"] = 25;
        return m;
    }

    future<map<any>> f = start timeoutFunction1(5000, m);
    map<anydata> waitedResult = (wait w3 | f);
    var result = waitedResult.clone();
    return result;
}

// Function used to provide timeout functionality
function timeoutFunction1(int milliSeconds, map<any> m) returns map<any> {
    runtime:sleep(milliSeconds);
    m["x"] = 15;
    return m;
}

function complexForkWorkerSendReceive() returns map<any> {
    map<any> m = {};
    m["x"] = 10;
    fork {
        worker w1 {
            int a = 5;
            int b = 0;
            a -> w2;
            b = <- w2;
        }
        worker w2 {
            int a = 0;
            int b = 15;
            a = <- w1;
            b -> w1;
        }
    }
    map<any> results = wait {w1, w2};
    m["x"] = 17;
    return m;
}

function chainedWorkerSendReceive() returns map<any> {
    map<any> m = {};
    fork {
        worker w1 {
            int a = 3;
            int b = 0;
            a -> w2;
            b = <- w3;
            m["x"] = b;
        }
        worker w2 {
            int a = 0;
            int b = 15;
            a = <- w1;
            a * 2 -> w3;
        }
        worker w3 {
            int a = 0;
            int b = 0;
            a = <- w2;
            b = a * 2;
            b -> w1;
        }
    }
    map<any> results = wait {w1, w2, w3};
    return m;
}

function forkWithWaitOnSomeSelectedWorkers1() returns int|error {
    map<any> m = {};
    m["x"] = 0;
    m["y"] = 0;
    fork {
        worker w1 {
            int a = 55;
            int b = 5;
            m["x"] = a;
            m["y"] = b;
        }
        worker w2 {
            int a = 5;
            int b = 15;
            runtime:sleep(2000);
            m["x"] = a;
        }
        worker w3 {
            int a = 0;
            int b = 15;
            runtime:sleep(1000);
            m["x"] = b;
        }
    }
    () results = wait w2 | w3;
    int j;
    int k;
    j = <int>m["x"];
    k = <int>m["y"];
    return j * k;
}

function forkWithWaitOnSomeSelectedWorkers2() returns map<any> {
    map<any> m = {};
    m["x"] = 0;
    fork {
        worker w1 {
            int x = 80;
            x -> w2;
            x = <- w3;
            m["x"] = x;
            10 -> w2;
        }
        worker w2 {
            int a = 0;
            a = <- w1;
            (a * 2) -> w3;
            a = <- w1;
            10 -> w3;
        }
        worker w3 {
            int a = 0;
            a = <- w2;
            (a * 2) -> w1;
            a = <- w2;
        }
    }
    () results = wait w1| w2 | w3;
    return m;
}

function forkWithWaitOnSomeSelectedWorkers3() returns map<any> {
    map<any> m = {};
    m["x"] = 0;
    fork {
        worker w1 {
            int x = 10;
            x -> w2;
            int a = 0;
            a = <- w3;
            (a * 2) -> w2;
        }
        worker w2 {
            int a = 0;
            a = <- w1;
            (a * 2) -> w3;
            a = <- w1;
            m["x"] = a;
            (a * 2) -> w3;
            runtime:sleep(1000);
        }
        worker w3 {
            int a = 0;
            a = <- w2;
            (a * 2) -> w1;
            m["x"] = <- w2;
        }
    }
    () results = wait w2 | w3;
    return m;
}

function forkWithWaitOnAllSelectedWorkers1() returns map<any> {
    map<any> m = {};
    m["x"] = 0;
    fork {
        worker w1 {
            int x = 10;
            x -> w2;
            int a = 0;
            a = <- w3;
            (a * 2) -> w2;
        }
        worker w2 {
            int a = 0;
            a = <- w1;
            (a * 2) -> w3;
            a = <- w1;
            m["x"] = a;
            (a * 2) -> w3;
            runtime:sleep(1000);
            m["x"] = 33;
        }
        worker w3 {
            int a = 0;
            a = <- w2;
            (a * 2) -> w1;
            m["x"] = <- w2;
        }
    }
    map<any> results = wait {w2, w3};
    return m;
}

function forkWithWaitOnAllSelectedWorkers2() returns int {
    int result = 0;
    fork {
        worker w1 {
            int x = 10;
            x -> w2;
            int a = 0;
            a = <- w3;
            (a * 2) -> w2;
        }
        worker w2 {
            int a = 0;
            a = <- w1;
            (a * 2) -> w3;
            a = <- w1;
            result = a;
            (a * 2) -> w3;
            runtime:sleep(2000);
            result = 33;
        }
        worker w3 {
            int a = 0;
            a = <- w2;
            (a * 2) -> w1;
            result = <- w2;
        }
    }
    worker w4 returns int {
        map<any> results = wait {w2, w3};
        return result;
    }

    future<int> f = start timeoutFunction2(1000);
    return (wait w4 | f);
}

// Function used to provide timeout functionality
function timeoutFunction2(int milliSeconds) returns int {
    runtime:sleep(milliSeconds);
    return 777;
}

function forkWithMessagePassing() returns map<any>|error {
    map<any> m = {};
    fork {
        worker w1 returns int {
            int a = 5;
            a -> w2;
            int b = 0;
            b = <- w2;
            return b;
        }
        worker w2 returns int {
            int a = 0;
            a = <- w1;
            int b = 15;
            b -> w1;
            return a;
        }
    }
    map<any> results = wait {w1, w2};
    int b = <int>results["w1"];
    int a = <int>results["w2"];

    m["x"] = (a + 1) * b;
    return m;
}

function forkWithinWorkers() returns int|error {
    worker wx returns int|error {
        int x = 20;
        map<any> m = {};
        fork {
            worker wx1 {
                m["a"] = 10;
            }
            worker wx2 {
                m["b"] = 20;
            }
        }
        map<any> results = wait {wx1, wx2};
        int a;
        int b;
        a = <int>m["a"];
        b = <int>m["b"];
        x = a + b;
        return x;
    }
    return wait wx;
}

function largeForkCreationTest() returns int|error {
    int result = 0;
    map<any> m = {};
    int c = 1000;
    while (c > 0) {
        m["x"] = 10;
        fork {
            worker w1 {
                int a = 2;
                int b = 0;
                a -> w2;
                b = <- w10;
                m["x"] = result + b;
            }
            worker w2 {
                int a = 0;
                int b = 3;
                a = <- w1;
                (a + b) -> w3;
            }
            worker w3 {
                int a = 0;
                int b = 4;
                a = <- w2;
                (a + b) -> w4;
            }
            worker w4 {
                int a = 0;
                int b = 5;
                a = <- w3;
                (a + b) -> w5;
            }
            worker w5 {
                int a = 0;
                int b = 6;
                a = <- w4;
                (a + b) -> w6;
            }
            worker w6 {
                int a = 0;
                int b = 7;
                a = <- w5;
                (a + b) -> w7;
            }
            worker w7 {
                int a = 0;
                int b = 8;
                a = <- w6;
                (a + b) -> w8;
            }
            worker w8 {
                int a = 0;
                int b = 9;
                a = <- w7;
                (a + b) -> w9;
            }
            worker w9 {
                int a = 0;
                int b = 10;
                a = <- w8;
                (a + b) -> w10;
            }
            worker w10 {
                int a = 0;
                int b = 11;
                a = <- w9;
                (a + b) -> w1;
            }
        }

        map<any> results = wait {w1, w2, w3, w4, w5, w6, w7, w8, w9, w10};
        result = <int>m["x"];
        c = c - 1;
    }
    return result;
}

function forkWithStruct() returns string|error {
    string result = "";
    fork {
        worker w1 returns foo {
            foo f = { x: 1, y: "w1" };
            return f;
        }
        worker w2 returns float {
            float f = 10.344;
            return f;
        }
    }
    map<any> results = wait {w1, w2};
    var f = <foo>results["w1"];
    result = "[block] sW1: " + f.y;
    var fW2 = <float>results["w2"];
    result = result + "[block] fW2: " + fW2.toString();
    return result;
}

type foo record {
    int x = 0;
    string y = "";
};

function forkWithSameWorkerContent() returns string|error {
    string result = "";
    fork {
        worker w1 returns any[] {
            any[] a = [];
            return a;
        }
        worker w2 returns any[] {
            any[] b = [];
            return b;
        }

    }
    map<any> results1 = wait {w1, w2};

    fork {
        worker w3 returns string[] {
            string[] a = ["data1"];
            return a;
        }
        worker w4 returns string[] {
            string[] a = ["data2"];
            return a;
        }
    }
    map<string[]> results2 = wait {w3, w4};
    string[]? resW1 = results2["w3"];
    var s1 = "";
    if(resW1 is string[]) {
        s1 = resW1[0];
    }
    result = "W3: " + s1;
    string[]? resW2 = results2["w4"];
    var s2 = "";
    if(resW2 is string[]) {
        s2 = resW2[0];
    }
    result = result + ", W4: " + s2;

    return result;
}

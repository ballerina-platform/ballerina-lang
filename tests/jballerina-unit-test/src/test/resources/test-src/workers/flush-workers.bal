import ballerina/jballerina.java;
string append = "";
function singleFlush () returns string {
    @strand{thread:"any"}
    worker w1 {
        int a = 10;
        a -> w2;
        error? result = flush w2;
        foreach var i in 1 ... 5 {
            append = append + "w1";
        }
    }
    @strand{thread:"any"}
    worker w2 {
        foreach var i in 1 ... 5 {
            append = append + "w2";
        }
        int b;
        b = <- w1;
    }

    wait w1;
    return append;
}

string append1 = "";
function flushReturn() returns error? {
    @strand{thread:"any"}
    worker w1 returns error? {
            int a = 10;
            a -> w2;
            a -> w2;
            error? result = flush w2;
            foreach var i in 1 ... 5 {
                append1 = append1 + "w1";
            }
            return result;
        }
        @strand{thread:"any"}
        worker w2 {
            foreach var i in 1 ... 5 {
                append1 = append1 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
        }

        var result = wait w1;
        return result;
}

string append2 = "";
function flushAll() returns string {
    @strand{thread:"any"}
    worker w1 {
            int a = 10;

            var sync = a ->> w2;
            any|error res = sync;
            a -> w3;
            a -> w2;
            error? result = flush;
            foreach var i in 1 ... 5 {
                append2 = append2 + "w1";
            }
        }
        @strand{thread:"any"}
        worker w2 returns error?{
            if(0 > 1){
                 error err = error("err", message = "err msg");
                 return err;
            }
            sleep(5);
            foreach var i in 1 ... 5 {
                append2 = append2 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
            return;
        }
        @strand{thread:"any"}
        worker w3 {
            sleep(5);
            foreach var i in 1 ... 5 {
                            append2 = append2 + "w3";
                        }
                        int b;
                        b = <- w1;
        }

         wait w1;
        return append2;
}

function errorTest() {
    var inner = function () returns error? {
        @strand{thread:"any"}
        worker w1 returns error?{
                int a = 10;

                var sync = a ->> w2;
                any|error res = sync;
                a -> w3;
                a -> w2;
                error? result = flush;
                foreach var i in 1 ... 5 {
                    append2 = append2 + "w1";
                }
                return result;
            }
            @strand{thread:"any"}
            worker w2 returns error?{
                if(0 > 1){
                     error err = error("err", message = "err msg");
                     return err;
                }
                sleep(5);
                foreach var i in 1 ... 5 {
                    append2 = append2 + "w2";
                }
                int b;
                b = <- w1;
                b = <- w1;
                return;
            }
            @strand{thread:"any"}
            worker w3 returns error|string{
                sleep(5);
                int k;
                foreach var i in 1 ... 5 {
                    append2 = append2 + "w3";
                    k = i;
                }
                if(k>3) {
                    map<string> reason = { k1: "error3" };
                        error er3 = error(reason.get("k1"),  message = "msg3");
                        return er3;
                    }

                int b;
                b = <- w1;
                return "done";
            }

            error? res = wait w1;
            return res;
    };
    validateError(inner(), "error3");
}

function panicTest() returns error? {
    @strand{thread:"any"}
    worker w1 returns error?{
            int a = 10;

            var sync = a ->> w2;
            any|error res = sync;
            a -> w3;
            a -> w2;
            error? result = flush;
            foreach var i in 1 ... 5 {
                append2 = append2 + "w1";
            }
            return result;
        }
        @strand{thread:"any"}
        worker w2 returns error?{
            if(0 > 1){
                 error err = error("err", message = "err msg");
                 return err;
            }
            sleep(5);
            foreach var i in 1 ... 5 {
                append2 = append2 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
            return;
        }
        @strand{thread:"any"}
        worker w3 returns error|string{
            sleep(5);
            int k;
            foreach var i in 1 ... 5 {
                append2 = append2 + "w3";
                k = i;
            }
            if(k>3) {
                map<string> reason = { k1: "error3" };
                    error er3 = error(reason.get("k1"), message = "msg3");
                    panic er3;
                }

            int b;
            b = <- w1;
            return "done";
        }

        error? res = wait w1;
        return res;
}

function flushInDefaultError() {
    var inner = function () returns error? {
        @strand{thread:"any"}
        worker w2 returns error? {
            int a = 0;
            int b = 15;
            if (0 < 1) {
                error err = error("err", message = "err msg");
                return err;
            }
            a = <- function;
            b = a + b;
            b -> function;
            return ;
        }
        int a = 10;
        a -> w2;
        error? res = flush;
        error|int c = <- w2;
        return res;
    };
    validateError(inner(), "err");
}

function flushInDefault() returns int {
   @strand{thread:"any"}
   worker w2 {
     int a = 0;
     int b = 15;
     a = <- function;
     b = a + b;
     b -> function;
   }
   int a = 10;
    a -> w2;
    error? res = flush;
    int c = <- w2;
    return c;
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

function validateError(any|error value, string message) {
    if (value is error) {
        if (value.message() == message) {
            return;
        }
        panic error("Expected error message: " + message + ", found: " + value.message());
    }
    panic error("Expected error, found: " + (typeof value).toString());
}

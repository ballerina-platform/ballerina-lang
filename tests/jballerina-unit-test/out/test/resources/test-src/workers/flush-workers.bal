import ballerina/runtime;
string append = "";
function singleFlush () returns string {

    worker w1 {
        int a = 10;
        a -> w2;
        error? result = flush w2;
        foreach var i in 1 ... 5 {
            append = append + "w1";
        }
    }

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
    worker w1 {
            int a = 10;

            var sync = a ->> w2;
            a -> w3;
            a -> w2;
            error? result = flush;
            foreach var i in 1 ... 5 {
                append2 = append2 + "w1";
            }
        }

        worker w2 returns error?{
            if(false){
                 error err = error("err", message = "err msg");
                 return err;
            }
            runtime:sleep(5);
            foreach var i in 1 ... 5 {
                append2 = append2 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
            return;
        }

        worker w3 {
            runtime:sleep(5);
            foreach var i in 1 ... 5 {
                            append2 = append2 + "w3";
                        }
                        int b;
                        b = <- w1;
        }

         wait w1;
        return append2;
}

function errorTest() returns error? {
    worker w1 returns error?{
            int a = 10;

            var sync = a ->> w2;
            a -> w3;
            a -> w2;
            error? result = flush;
            foreach var i in 1 ... 5 {
                append2 = append2 + "w1";
            }
            return result;
        }

        worker w2 returns error?{
            if(false){
                 error err = error("err", message = "err msg");
                 return err;
            }
            runtime:sleep(5);
            foreach var i in 1 ... 5 {
                append2 = append2 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
            return;
        }

        worker w3 returns error|string{
            runtime:sleep(5);
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
}

function panicTest() returns error? {
    worker w1 returns error?{
            int a = 10;

            var sync = a ->> w2;
            a -> w3;
            a -> w2;
            error? result = flush;
            foreach var i in 1 ... 5 {
                append2 = append2 + "w1";
            }
            return result;
        }

        worker w2 returns error?{
            if(false){
                 error err = error("err", message = "err msg");
                 return err;
            }
            runtime:sleep(5);
            foreach var i in 1 ... 5 {
                append2 = append2 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
            return;
        }

        worker w3 returns error|string{
            runtime:sleep(5);
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

function flushInDefaultError() returns error? {
   worker w2 returns error? {
     int a = 0;
     int b = 15;
     if (true) {
       error err = error("err", message = "err msg");
              return err;
     }
     a = <- default;
     b = a + b;
     b -> default;
     return ;
   }
   int a = 10;
    a -> w2;
    error? res = flush;
    error|int c = <- w2;
    return res;
}

function flushInDefault() returns int {
   worker w2 {
     int a = 0;
     int b = 15;
     a = <- default;
     b = a + b;
     b -> default;
   }
   int a = 10;
    a -> w2;
    error? res = flush;
    int c = <- w2;
    return c;
}

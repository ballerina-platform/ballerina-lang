function workerReturnTest() returns int{
    worker wx returns int {
	    int x = 50;
	    return x + 1;
    }

    return (wait wx) + 1;
}

function workerSendToDefault() returns int{
    worker w1 {
        int x = 50;
        x -> default;
    }
    int y = <- w1;
    return y + 1;
}

function workerSendFromDefault() returns int{
    worker w1 returns int {
        int y = <- default;
        return y;
    }
    int x = 50;
    x -> w1;

    return (wait w1) + 1;
}

function workerTestWithLambda() returns int {
    invokeTestFunc(5);
    (function () returns (int)) fa = function () returns (int) { return 88; };
    int a = fa.call();
    return a;
}



function invokeTestFunc(int c) {
    worker w1 returns int {
        int a = <- default;
        return a;
    }
    int b = 9;
    b -> w1;
}

public function workerSendToWorker() returns int {
    worker w1 {
      int i = 40;
      i -> w2;
    }

    worker w2 returns int {
      int j = 25;
      j = <- w1;

      return j;
    }

    return (wait w2) + 1;
}

public function receiveWithCheck() returns error|int {
    worker w1 returns boolean|error{
      int i = 2;
      if(true){
           error err = error("err", { message: "err msg" });
           return err;
      }
      i -> w2;
      return false;
    }

    worker w2 returns error?{
      int j = check <- w1;
      return;
    }

    return wait w2;
}

public function receiveWithCheckForDefault() returns boolean|error {
    worker w1 returns boolean|error {
        int i = 2;
        if(true){
            error err = error("err from panic");
            return err;
        }
        i -> default;
        return false;
    }

    error|int j = check <- w1;
    return wait w1;
}

int res = 0;
function simpleSyncSend() returns int {
    string done = process();
    return res;
}

function process() returns string {
   worker w1 {
     int a = 10;
     a -> w2;
     () result = a ->> w2;
     int i = 0;
     while (i < 1000) {
         res = 10;
         i += 1;
     }
     a -> w2;

    }

   worker w2 {
     int b = 15;
     b = <- w1;
     int i = 0;
     while (i < 1000) {
          res = 10;
          i += 1;
     }

     b = <- w1;
     b = <- w1;
   }

   wait w1;

   return "done";
}

string append2 = "";
function multipleSyncSend() returns string{
    worker w1 {
        int a = 10;
        var result = a ->> w2;
        int i = 0;
        while (i < 5) {
            append2 = append2 + "w1";
            i += 1;
        }
        result = a ->> w2;
        i = 0;
        while (i < 5) {
            append2 = append2 + "w11";
            i += 1;
        }
    }

    worker w2 returns error? {
        int b = 15;
        int i = 0;
        while (i < 5) {
            append2 = append2 + "w2";
            i += 1;
        }
        if (false) {
            error err = error("err", { message: "err msg" });
            return err;
        }
        b = <- w1;
        i = 0;
        while (i < 5) {
            append2 = append2 + "w22";
            i += 1;
        }
        b = <- w1;
        return;
    }
    wait w1;
    return append2;
}

string append = "";
function process2() returns any|error {
    return returnNil();
}

function returnNil() returns any|error {
    worker w1 returns any|error {
        int a = 10;
        a -> w2;
        var result = a ->> w2;
        int i = 0;
        while (i < 5) {
            append = append + "w1";
            i += 1;
        }
        return result;
    }

   worker w2 returns error? {
    if (false) {
        error err = error("err", { message: "err msg" });
        return err;
    }
    int b = 15;

    int i = 0;
    while (i < 5) {
        append = append + "w2";
        i += 1;
    }
    b = <- w1;
    b = <- w1;
    return;
   }

   var result = wait w1;
   return result;
}

string append3 = "";
function multiWorkerSend() returns string{
    worker w1 {
        int a = 10;
        var result = a ->> w2;
        a -> w3;
        int i = 0;
        while (i < 5) {
            append3 = append3 + "w1";
            i += 1;
        }
        result = a ->> w2;
        a -> w3;
        i = 0;
        while (i < 5) {
            append3 = append3 + "w11";
            i += 1;
        }
       }

    worker w2 returns error? {
         if (false) {
            error err = error("err", { message: "err msg" });
            return err;
         }

        if (false) {
            error err = error("err", { message: "err msg" });
            return err;
        }
        int b = 15;
        int i = 0;
        while (i < 5) {
            append3 = append3 + "w2";
            i += 1;
        }
        b -> w3;
        b = <- w1;
        b -> w3;
        i = 0;
        while (i < 5) {
            append3 = append3 + "w22";
            i += 1;
        }
        b = <- w1;
        return;
    }


    worker w3 returns error? {
        int|error x =  <- w2;
        int b;
        int i = 0;
            while (i < 5)  {
                append3 = append3 + "w3";
                i += 1;
            }
        b = <- w1;
        x = <- w2;
        i = 0;
        while (i < 5) {
            append3 = append3 + "w33";
            i += 1;
        }
        b = <- w1;
        return;
    }

    wait w1;
    return append3;
}

string append4 = "";
function errorResult() returns error? {
    worker w1 returns error? {
        int a = 10;
        if (false) {
            error err = error("err", { message: "err msg" });
            return err;
        }
        var result = a ->> w2;

        return result;
    }

    worker w2 returns error? {
        if (true) {
            error err = error("error3", { message: "err msg" });
            return err;
        }
        var b = 15;

        b = <- w1;
        return;
    }

    error? w1Result = wait w1;
    return w1Result;
}

string fappend = "";
function singleFlush () returns string {

    worker w1 {
        int a = 10;
        a -> w2;
        error? result = flush w2;
        int i = 0;
        while(i < 5) {
            i += 1;
            fappend = fappend + "w1";
        }
    }

    worker w2 {
        int i = 0;
        while(i < 5) {
            i += 1;
            fappend = fappend + "w2";
        }
        int b;
        b = <- w1;
    }

    wait w1;
    return fappend;
}

string fappend3 = "";
function flushReturn() returns error? {
    worker w1 returns error? {
            int a = 10;
            a -> w2;
            a -> w2;
            error? result = flush w2;
            int i = 0;
            while (i < 5) {
                i += 1;
                fappend3 = fappend3 + "w1";
            }
            return result;
        }

        worker w2 {
            int i = 0;
            while (i < 5) {
                i += 1;
                fappend3 = fappend3 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
        }

        var result = wait w1;
        return result;
}

string fappend2 = "";
function flushAll() returns string {
    worker w1 {
            int a = 10;

            var sync = a ->> w2;
            a -> w3;
            a -> w2;
            error? result = flush;
            int i = 0;
            while (i < 5) {
                i += 1;
                fappend2 = fappend2 + "w1";
            }
        }

        worker w2 returns error?{
            if(false){
                 error err = error("err", { message: "err msg" });
                 return err;
            }
            
            int i = 0;
            while (i < 5) {
                i += 1;
                fappend2 = fappend2 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
            return;
        }

        worker w3 {
            int i = 0;
            while (i < 5) {
                i += 1;
                fappend2 = fappend2 + "w3";
            }
            int b;
            b = <- w1;
        }

         wait w1;
        return fappend2;
}

string fappend4 = "";
function errorTest() returns error? {
    worker w1 returns error?{
            int a = 10;

            var sync = a ->> w2;
            a -> w3;
            a -> w2;
            error? result = flush;
            int i = 0;
            while (i < 5) {
                i += 1;
                fappend4 = fappend4 + "w1";
            }
            return result;
        }

        worker w2 returns error?{
            if(false){
                 error err = error("err", { message: "err msg" });
                 return err;
            }
            int i = 0;
            while (i < 5) {
                i += 1;
                fappend4 = fappend4 + "w2";
            }
            int b;
            b = <- w1;
            b = <- w1;
            return;
        }

        worker w3 returns error|string{
            int k;
            int i = 0;
            while (i < 5) {
                i += 1;
                fappend4 = fappend4 + "w3";
                k = i;
            }
            if (k > 3) {
                map<string> reason = { k1: "error3" };
                map<string> details = { message: "msg3" };
                    error er3 = error(reason.k1, details);
                    return er3;
                }

            int b;
            b = <- w1;
            return "done";
        }

        error? result = wait w1;
        return result;
}



function flushInDefaultError() returns error? {
   worker w2 returns error? {
     int a = 0;
     int b = 15;
     if (true) {
       error err = error("err", { message: "err msg" });
              return err;
     }
     a = <- default;
     b = a + b;
     b -> default;
     return ;
   }
   int a = 10;
    a -> w2;
    error? result = flush;
    error|int c = <- w2;
    return result;
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
    error? result = flush;
    int c = <- w2;
    return c;
}

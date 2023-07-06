import ballerina/jballerina.java;

function workerReturnTest() returns int {
    @strand{thread:"any"}
    worker wx returns int {
	    int x = 50;
	    return x + 1;
    }
    int res = wait wx;
    return res + 1;
}

public function workerSendToWorker() returns int {
    @strand{thread:"any"}
    worker w1 {
      int i = 40;
      i -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns int {
      int j = 25;
      j = <- w1;

      return j;
    }
    int ret = wait w2;

    return ret + 1;
}

function workerSendToDefault() returns int{
    @strand{thread:"any"}
    worker w1 {
        int x = 50;
        x -> function;
    }
    int y = <- w1;
    return y + 1;
}

function workerSendFromDefault() returns int{
    @strand{thread:"any"}
    worker w1 returns int {
        int y = <- function;
        return y;
    }
    int x = 50;
    x -> w1;
    int res = wait w1;
    return res + 1;
}

public function receiveWithTrap() {
   @strand{thread:"any"}
   worker w1 {
     int i = 2;
     if(0 < 1) {
          error err = error("err", message = "err msg");
          panic err;
     }
     i -> w2;
   }

   @strand{thread:"any"}
   worker w2 returns error|int {
     error|int  j = trap <- w1;
     return j;
   }

   error|int ret = wait w2;

   validateError(ret, "err");
}

public function syncSendReceiveWithTrap() {
    var f = function () returns int|string|error {
        @strand{thread:"any"}
        worker w1 {
            int|string i = 2;
            if i is int {
                panic error("sync send err", message = "err msg");
            }
            i ->> w2;
       }

        @strand{thread:"any"}
        worker w2 returns error|int|string {
            int|string|error  j = trap <- w1;
            return j;
        }

       int|string|error ret = wait w2;
       return ret;
    };

    validateError(f(), "sync send err");
}

public function receiveWithCheck() {
    @strand{thread:"any"}
    worker w1 returns boolean|error{
      int i = 2;
      if(0 < 1){
           error err = error("err", message = "err msg");
           return err;
      }
      i -> w2;
      return false;
    }

    @strand{thread:"any"}
    worker w2 returns error?{
      int j = check <- w1;
      return;
    }

    var r = wait w2;
    validateError(r, "err");
}

public function syncSendReceiveWithCheck() {
    @strand{thread:"any"}
    worker w1 returns boolean|error {
        int i = 2;
        if (0 < 1) {
            return error("sync send err", message = "err msg");
        }
        i -> w2;
        return false;
    }

    @strand{thread:"any"}
    worker w2 returns error? {
        int j = check <- w1;
    }

    var r = wait w2;
    validateError(r, "sync send err");
}

public function receiveWithCheckpanic() {
    @strand{thread:"any"}
    worker w1 returns boolean|error {
        int i = 2;
        if (0 < 1) {
            error err = error("err", message = "err msg");
            return err;
        }
        i -> w2;
        return false;
    }

    @strand{thread:"any"}
    worker w2 {
        int j = checkpanic <- w1;
    }

    wait w2;
}

public function syncSendReceiveWithCheckpanic() {
    @strand{thread:"any"}
    worker w1 returns boolean|error {
        int i = 2;
        if (0 < 1) {
            error err = error("err", message = "sync send err msg");
            return err;
        }
        i ->> w2;
        return false;
    }

    @strand{thread:"any"}
    worker w2 {
        int j = checkpanic <- w1;
    }

    wait w2;
}

public function sendToDefaultWithPanicBeforeSendInWorker() returns int {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        if(0 < 1) {
            error err = error("error: err from panic");
            panic err;
        }
        i -> function;
    }
    int res = <- w1;
    wait w1;
    return res;
}

public function sendToDefaultWithPanicBeforeSendInDefault() returns int {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i ->> function;
    }
    if(0 < 1) {
        error err = error("error: err from panic");
        panic err;
    }
    int res = <- w1;
    wait w1;
    return res;
}

public function sendToDefaultWithPanicAfterSendInWorker() returns int {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        error err = error("error: err from panic");
        i -> function;
        if(0 < 1) {
            panic err;
        }
        i -> function;
    }

    int res = <- w1;
    res = <- w1;
    return res;
}

public function sendToDefaultWithPanicAfterSendInDefault() returns int {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i -> function;
    }
    int res = <- w1;
    if(0 < 1) {
        error err = error("error: err from panic");
        panic err;
    }
    return res;
}

public function receiveFromDefaultWithPanicAfterSendInDefault() {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i = <- function;
    }
    int sq = 16;
    sq -> w1;
    if(0 < 1) {
        error err = error("error: err from panic");
        panic err;
    }
}

public function receiveFromDefaultWithPanicBeforeSendInDefault() {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i = <- function;
    }
    if(0 < 1) {
        error err = error("error: err from panic");
        panic err;
    }
    int sq = 16;
    sq -> w1;
}

public function receiveFromDefaultWithPanicBeforeReceiveInWorker() {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        if(0 < 1) {
            error err = error("error: err from panic");
            panic err;
        }
        i = <- function;
    }
    int sq = 16;
    sq -> w1;
    wait w1;
}

public function receiveFromDefaultWithPanicAfterReceiveInWorker() {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i = <- function;
        if(0 < 1) {
            error err = error("error: err from panic");
            panic err;
        }
    }
    int sq = 16;
    sq -> w1;
    wait w1;
}

public function receiveWithCheckAndTrap() {
   @strand{thread:"any"}
   worker w1 {
       int i = 2;
       if(0 < 1) {
           error err = error("error: err from panic");
           panic err;
       }
       i -> w2;
   }

   @strand{thread:"any"}
   worker w2 returns error|int {
       error|int  j = check trap <- w1;
       return j;
   }

   var r = wait w2;
   validateError(r, "error: err from panic");
}

public function receiveWithCheckForDefault() {
    var f = function () returns boolean|error {
        @strand{thread:"any"}
        worker w1 returns boolean|error {
            int i = 2;
            if(0 < 1){
                error err = error("err from panic");
                return err;
            }
            i -> function;
            return false;
        }

        error|int j = check <- w1;
        return wait w1;
    };
   validateError(f(), "err from panic");
}

public function receiveWithTrapForDefault() {
    var f = function () returns error|int {
        @strand{thread:"any"}
        worker w1 returns int {
           int i = 2;
           if(0 < 1) {
               error err = error("error: err from panic");
               panic err;
           }
           i -> function;
           return i;
        }

        error|int  j = trap <- w1;
        return j;
    };
    validateError(f(), "error: err from panic");
}

public function receiveDefaultWithCheckAndTrap() {
    var f = function () returns error|int? {
       @strand{thread:"any"}
       worker w1 {
           int i = 2;
           if(0 < 1) {
               error err = error("error: err from panic");
               panic err;
           }
           i -> function;
       }

       error|int j = check trap <- w1;
       return j;
    };
    validateError(trap f(), "error: err from panic");
}

function workerTestWithLambda() returns int {
    invokeTestFunc(5);
    int a = fa();
    return a;
}

(function () returns (int)) fa = function () returns (int) { return 88; };

function invokeTestFunc(int c) {
    @strand{thread:"any"}
    worker w1 returns int {
        int a = <- function;
        return a;
    }
    int b = 9;
    b -> w1;
}

public type Rec record {
    int k = 0;
};

public function testComplexType() returns Rec {
    @strand{thread:"any"}
    worker w1 {
      Rec rec = {};
      rec.k = 10;
      //int i = 40;
      rec -> w2;
      rec.k = 50;
      5 -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns Rec {
      int l = 25;
      Rec j = {};
      j = <- w1;
      l = <- w1;
      return j;
    }

    return wait w2;
}

// First cancel the future and then wait
public function workerWithFutureTest1() returns int|error {
    future<int> f1 = @strand{thread:"any"} start add2(5, 5);
    @strand{thread:"any"}
    worker w1 {
      int i = 40;
      f1.cancel();
    }

    @strand{thread:"any"}
    worker w2 returns int|error {
      // Delay the execution of worker w2
      sleep(1000);
      int|error i = wait f1;
      return i;
    }

    return wait w2;
}

// First wait on the future and then cancel
public function workerWithFutureTest2() returns int|error {
    future<int> f1 = @strand{thread:"any"} start add(6, 6);
    @strand{thread:"any"}
    worker w1 {
      int i = 40;
      // Delay the execution of worker w1
      sleep(1000);
      f1.cancel();
    }

    @strand{thread:"any"}
    worker w2 returns int|error {
      int|error i = wait f1;
      return i;
    }
    return wait w2;
}

// Concurrently run cancel in worker w1 and wait in worker w2
public function workerWithFutureTest3() returns int {
    future<int> f1 = start add(10, 8);
    @strand{thread:"any"}
    worker w1 {
      int i = 40;
      f1.cancel();
    }

    @strand{thread:"any"}
    worker w2 returns int {
      // Delay the execution of worker w1
      sleep(1000);
      int i = checkpanic wait f1;
      return i;
    }
    return wait w2;
}

function add(int i, int j) returns int {
  waitFor();
  return i + j;
}

function waitFor() {
   int l = 0;
   while (l < 99999) {
    l = l +1;
   }
}

function add2(int i, int j) returns int {
   sleep(1000);
   int l = 0;
   while (l < 99999) {
     l = singleAdd(l);
   }
   return i + j;
}

function singleAdd(int num) returns int{
   return num +1;
}

function innerWorkerPanicTest() {
   @strand{thread:"any"}
   worker w1 {
       int k = <- function;
   }

   panicFunc();

   10 -> w1;
}

function panicFunc() {
    @strand{thread:"any"}
    worker w5 {
       if (0 < 1) {
           error e = error("worker w5 panic");
           panic e;
       }
       10 -> function;
    }
    int k = <- w5;
}

function waitInReturn() returns any {
    @strand{thread:"any"}
    worker w1 returns string {
        return "w1";
    }

    @strand{thread:"any"}
    worker w2 returns string {
        return "w2";
    }

    return wait {w1, w2};
}

function testPanicWorkerInsideLock() {
    lock {
        panicWorkerInsideLock();
    }
}

function panicWorkerInsideLock() {
    worker w1 {
        int i = 5;
    }
}

function testPanicWorkerInsideLockWithDepth3() {
    lock {
        testPanicWorkerInsideLockWithDepth2();
    }
}

function testPanicWorkerInsideLockWithDepth2() {
    testPanicWorkerInsideLockWithDepth1();
}

function testPanicWorkerInsideLockWithDepth1() {
    worker w1 {
        int i = 5;
    }
}

function testPanicStartInsideLock() {
    lock {
        panicStartInsideLock();
    }
}

function panicStartInsideLock() {
    _ = start testStartFunction();
}

function testPanicStartInsideLockWithDepth3() {
    lock {
        testPanicStartInsideLockWithDepth2();
    }
}

function testPanicStartInsideLockWithDepth2() {
    testPanicStartInsideLockWithDepth1();
}

function testPanicStartInsideLockWithDepth1() {
    _ = start testStartFunction();
}

function testStartFunction() {
    int i = 4;
}

function () returns int sumFunction =
        function () returns int {
            worker w1 {
                1 -> w2;
            }

            worker w2 returns int {
                int j = <- w1;
                return j;
            }

            int k = wait w2;
            return k;
        };

function testLambdaWithWorkerMessagePassing() {
    int k = sumFunction();
    if (k != 1) {
        panic error("Assertion error: expected 1, found: " + k.toString());
    }
}

function workerInteractionAfterCheckExpr(boolean errorInBar) returns int|error {
    worker A returns (error)|string? {
        check foo();
        42 -> function;
    }

    worker B returns Error? {
        check bar(errorInBar);
        42 -> function;
    }

    int x = check <- A;
    int y = check <- B;
    return x;
}

function foo() returns error? => ();

type Error distinct error;

function bar(boolean b) returns Error? {
    return b ? error("Error") : ();
}

function testWorkerInteractionsAfterCheck() {
    var r = workerInteractionAfterCheckExpr(false);
    if !(r is int && r == 42) {
        panic error("Assertion error: expected `42` found: " + (typeof(r)).toString());
    }

    var e = workerInteractionAfterCheckExpr(true);
    validateError(e, "Error");
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

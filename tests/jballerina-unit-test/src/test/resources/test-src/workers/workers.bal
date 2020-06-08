import ballerina/io;
import ballerina/runtime;

function workerReturnTest() returns int{
    @strand{thread:"any"}
    worker wx returns int {
	    int x = 50;
	    return x + 1;
    }
    int res = wait wx;
    return res + 1;
}

int updateMultiple = 0;
function waitOnSameFutureByMultiple() returns int {
    @strand{thread:"any"}
    worker w1 returns int {
        return 9;
    }

    waitOnSameFutureWorkers(w1);
    runtime:sleep(1000);
    return updateMultiple;

}

function waitOnSameFutureWorkers(future<int> aa) {

    @strand{thread:"any"}
    worker w1 {
        int result = wait aa;
        lock {
        updateMultiple = updateMultiple + result;
        }
    }
    @strand{thread:"any"}
    worker w2 {
        int result = wait aa;
        lock {
        updateMultiple = updateMultiple + result;
        }
    }

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

    io:println(j);
      return j;
    }
    int ret = wait w2;

    return ret + 1;
}

function workerSendToDefault() returns int{
    @strand{thread:"any"}
    worker w1 {
        int x = 50;
        x -> default;
    }
    int y = <- w1;
    return y + 1;
}

function workerSendFromDefault() returns int{
    @strand{thread:"any"}
    worker w1 returns int {
        int y = <- default;
        return y;
    }
    int x = 50;
    x -> w1;
    int res = wait w1;
    return res + 1;
}

public function receiveWithTrap() returns error|int {
   @strand{thread:"any"}
   worker w1 {
     int i = 2;
     if(true) {
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

   return ret;
}

public function syncSendReceiveWithTrap() returns int|error {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        if true {
            panic error("sync send err", message = "err msg");
        }
        i ->> w2;
   }

    @strand{thread:"any"}
    worker w2 returns error|int {
        int|error  j = trap <- w1;
        return j;
    }

   int|error ret = wait w2;
   return ret;
}

public function receiveWithCheck() returns error|int {
    @strand{thread:"any"}
    worker w1 returns boolean|error{
      int i = 2;
      if(true){
           error err = error("err", message = "err msg");
           return err;
      }
      i -> w2;
      io:println("w1");
      return false;
    }

    @strand{thread:"any"}
    worker w2 returns error?{
      int j = check <- w1;
      return;
    }

    return wait w2;
}

public function syncSendReceiveWithCheck() returns int|error {
    @strand{thread:"any"}
    worker w1 returns boolean|error {
        int i = 2;
        if (true) {
            return error("sync send err", message = "err msg");
        }
        i -> w2;
        return false;
    }

    @strand{thread:"any"}
    worker w2 returns error? {
        int j = check <- w1;
    }

    return wait w2;
}

public function receiveWithCheckpanic() {
    @strand{thread:"any"}
    worker w1 returns boolean|error {
        int i = 2;
        if (true) {
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
        if (true) {
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
        if(true) {
            error err = error("error: err from panic");
            panic err;
        }
        i -> default;
    }
    wait w1;
    int res = <- w1;
    return res;
}

public function sendToDefaultWithPanicBeforeSendInDefault() returns int {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i -> default;
    }
    wait w1;
    if(true) {
        error err = error("error: err from panic");
        panic err;
    }
    int res = <- w1;
    return res;
}

public function sendToDefaultWithPanicAfterSendInWorker() returns int {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i -> default;
        if(true) {
            error err = error("error: err from panic");
            panic err;
        }
    }
    wait w1;
    int res = <- w1;
    return res;
}

public function sendToDefaultWithPanicAfterSendInDefault() returns int {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i -> default;
    }
    int res = <- w1;
    if(true) {
        error err = error("error: err from panic");
        panic err;
    }
    return res;
}

public function receiveFromDefaultWithPanicAfterSendInDefault() {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i = <- default;
    }
    int sq = 16;
    sq -> w1;
    if(true) {
        error err = error("error: err from panic");
        panic err;
    }
}

public function receiveFromDefaultWithPanicBeforeSendInDefault() {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i = <- default;
    }
    if(true) {
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
        if(true) {
            error err = error("error: err from panic");
            panic err;
        }
        i = <- default;
    }
    int sq = 16;
    sq -> w1;
    wait w1;
}

public function receiveFromDefaultWithPanicAfterReceiveInWorker() {
    @strand{thread:"any"}
    worker w1 {
        int i = 2;
        i = <- default;
        if(true) {
            error err = error("error: err from panic");
            panic err;
        }
    }
    int sq = 16;
    sq -> w1;
    wait w1;
}

public function receiveWithCheckAndTrap() returns error|int {
   @strand{thread:"any"}
   worker w1 {
       int i = 2;
       if(true) {
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

   return wait w2;
}

public function receiveWithCheckForDefault() returns boolean|error {
    @strand{thread:"any"}
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
public function receiveWithTrapForDefault() returns error|int {
   @strand{thread:"any"}
   worker w1 returns int {
       int i = 2;
       if(true) {
           error err = error("error: err from panic");
           panic err;
       }
       i -> default;
       return i;
   }

   error|int  j = trap <- w1;
   return j;
}

public function receiveDefaultWithCheckAndTrap() returns error|int {
   @strand{thread:"any"}
   worker w1 {
       int i = 2;
       if(true) {
           error err = error("error: err from panic");
           panic err;
       }
       i -> default;
   }

   error|int j = check trap <- w1;
   return j;
}

int rs = 0;
public function sameStrandMultipleInvocation() {
    rs = 0;
    while rs < 2 {
        rs = rs + 1;
        test(rs + 10);
    }
    runtime:sleep(60);
    return;
}

function test(int c) {
    @strand{thread:"any"}
    worker w1 {
        int a = c;
        io:println("w1 begin ", c);
        if (c == 11) {
            io:println("w1 sleep ", c);
            runtime:sleep(20);
        }
        io:println("w1 send data ", c);
        a -> w2;
    }
    @strand{thread:"any"}
    worker w2 {
        io:println("w2 begin ", c);
        if (c == 12) {
            io:println("w2 sleep ", c);
            runtime:sleep(20);
        }
        int b = <- w1;
        io:println("w2 end ", c, " - ", b);
    }
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
        int a = <- default;
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
public function workerWithFutureTest1() returns int {
    future<int> f1 = @strand{thread:"any"} start add2(5, 5);
    @strand{thread:"any"}
    worker w1 {
      int i = 40;
      f1.cancel();
    }

    @strand{thread:"any"}
    worker w2 returns int {
      // Delay the execution of worker w2
      runtime:sleep(1000);
      int i = wait f1;
      return i;
    }

    return wait w2;
}

// First wait on the future and then cancel
public function workerWithFutureTest2() returns int {
    future<int> f1 = @strand{thread:"any"} start add(6, 6);
    @strand{thread:"any"}
    worker w1 {
      int i = 40;
      // Delay the execution of worker w1
      runtime:sleep(1000);
      f1.cancel();
    }

    @strand{thread:"any"}
    worker w2 returns int {
      int i = wait f1;
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
      runtime:sleep(1000);
      int i = wait f1;
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
       int k = <- default;
   }

   panicFunc();

   10 -> w1;
}

function panicFunc() {
    @strand{thread:"any"}
    worker w5 {
       if (true) {
           error e = error("worker w5 panic");
           panic e;
       }
       10 -> default;
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

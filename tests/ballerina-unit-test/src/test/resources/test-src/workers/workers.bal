import ballerina/io;

function workerReturnTest() returns int{
    worker wx returns int {
	    int x = 50;
	    return x + 1;
    }
    return (wait wx) + 1;
}


public function workerSendToWorker() returns int {
    worker w1 {
      int i = 40;
      i -> w2;
    }

    worker w2 returns int {
      int j = 25;
      j = <- w1;

    io:println(j);
      return j;
    }

    return (wait w2) + 1;
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

public function receiveWithTrap() returns error|int {
    worker w1 {
      int i = 2;
      if(true) {
           error err = error("err", { message: "err msg" });
           panic err;
      }
      i -> w2;
    }

    worker w2 returns error|int {
      error|int  j = trap <- w1;
      return j;
    }

    return wait w2;
}


public function receiveWithCheck() returns error|int {
    worker w1 returns boolean|error{
      int i = 2;
      if(true){
           error err = error("err", { message: "err msg" });
           return err;
      }
      i -> w2;
      io:println("w1");
      return false;
    }

    worker w2 returns error?{
      int j = check <- w1;
      return;
    }

    return wait w2;
}

public function sendToDefaultWithPanicBeforeSendInWorker() returns int {
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
    worker w1 {
        int i = 2;
        if(true) {
            error err = error("error: err from panic");
            panic err;
        }
        i -> w2;
    }

    worker w2 returns error|int {
        error|int  j = check trap <- w1;
        return j;
    }

    return wait w2;
}

//public function receiveWithCheckForDefault() returns boolean|error {
//    worker w1 returns boolean|error {
//        int i = 2;
//        if(true){
//            error err = error("error: err from panic");
//            return err;
//        }
//        i -> default;
//        return false;
//    }
//
//    error|boolean j = check <- w1;
//    return wait w1;
//}

public function receiveWithTrapForDefault() returns error|int {
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
    return wait w1;
}

public function receiveDefaultWithCheckAndTrap() returns error|int {
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

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

int append = 0;
function simpleSyncSend() returns int {
    string done = process();
    return append;
}

function process() returns string {
   worker w1 {
     int a = 10;
     a -> w2;
     () result = a ->> w2;
     int i = 0;
     while (i < 1000) {
         append = 10;
         i += 1;
     }
     a -> w2;

    }

   worker w2 {
     int b = 15;
     b = <- w1;
     int i = 0;
     while (i < 1000) {
          append = 10;
          i += 1;
     }

     b = <- w1;
     b = <- w1;
   }

   wait w1;

   return "done";
}

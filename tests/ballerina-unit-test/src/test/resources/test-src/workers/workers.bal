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

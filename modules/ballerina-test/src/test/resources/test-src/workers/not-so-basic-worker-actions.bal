import ballerina.lang.system;

function forkJoinWithTimeoutTest1() (map) {
    map m = {};
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     a -> w2;
	     b <- w2;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     a <- w1;
	     b -> w1;
	     system:sleep(5000);
	   }
    } join (all) (map results) { m["x"] = 25; } timeout (1) (map results) { m["x"] = 15; }
    return m;
}

function forkJoinWithTimeoutTest2() (map) {
    map m = {};
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     system:sleep(100);
	   }
    } join (all) (map results) { m["x"] = 25; } timeout (5) (map results) { m["x"] = 15; }
    return m;
}

function complexForkJoinWorkerSendReceive() (map) {
    map m = {};
    m["x"] = 10;
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     a -> w2;
	     b <- w2;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     a <- w1;
	     b -> w1;
	   }
    } join (all) (map results) { m["x"] = 17; }
    return m;
}

function chainedWorkerSendReceive() (map) {
    map m = {};
    fork {
	   worker w1 {
	     int a = 3;
	     int b = 0;
	     a -> w2;
	     b <- w3;
	     m["x"] = b;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     a <- w1;
	     a * 2 -> w3;
	   }
	   worker w3 {
	     int a = 0;
	     int b = 0;
	     a <- w2;
	     b = a * 2;
	     b -> w1;
	   }
    } join (all) (map results) { }
    return m;
}

function forkJoinWithSomeSelectedJoin1() (map) {
    map m = {};
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
	     system:sleep(2000);
	     m["x"] = a;
	   }
	   worker w3 {
	     int a = 0;
	     int b = 15;
         system:sleep(1000);
	     m["x"] = b;
	   }
	} join (some 1 w2, w3) (map results) {  system:println(results);  }
	return m;
}

function forkJoinWithSomeSelectedJoin2() (map) {
    map m = {};
    m["x"] = 0;
    fork {
	   worker w1 {
	     int x = 80;
	     x -> w2;
	     x <- w3;
	     m["x"] = x;
	     10 -> w2;
	   }
	   worker w2 {
	     int a = 0;
	     a <- w1;
	     (a * 2) -> w3;
	     a <- w1;
	     10 -> w3;
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     (a * 2) -> w1;
	     a <- w2;
	   }
	} join (some 1 w1, w2, w3) (map results) {  system:println(results);  }
	return m;
}

function forkJoinWithSomeSelectedJoin3() (map) {
    map m = {};
    m["x"] = 0;
    fork {
	   worker w1 {
	     int x = 10;
	     x -> w2;
	     int a = 0;
	     a <- w3;
	     (a * 2) -> w2;
	   }
	   worker w2 {
	     int a = 0;
	     a <- w1;
	     (a * 2) -> w3;
	     a <- w1;
	     m["x"] = a;
	     (a * 2) -> w3;
	     system:sleep(1000);
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     (a * 2) -> w1;
	     m["x"] <- w2;
	   }
	} join (some 1 w2, w3) (map results) {  system:println(results);  }
	return m;
}

function forkJoinWithSomeSelectedJoin4() (map) {
    map m = {};
    m["x"] = 0;
    fork {
	   worker w1 {
	     10 -> w2;
	   }
	   worker w2 {
	     int a = 0;
	     a <- w1;
	     a -> w3;
	     m["x"] = a;
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     system:sleep(1000);
	     m["x"] = a * 2;
	   }
	} join (some 2 w1, w2, w3) (map results) {  system:println(results);  }
	return m;
}

function forkJoinWithSomeSelectedJoin5() (map) {
    map m = {};
    m["x"] = 0;
    fork {
	   worker w1 {
	     10 -> w2;
	   }
	   worker w2 {
	     int a = 0;
	     a <- w1;
	     a -> w3;
	     m["x"] = a;
	     a <- w3;
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     system:sleep(5000);
	     m["x"] = a * 2;
	     a -> w2;
	   }
	} join (some 2 w1, w2, w3) (map results) { } timeout (1) (map results) {  m["x"] = 555;  }
	return m;
}

function forkJoinWithAllSelectedJoin1() (map) {
    map m = {};
    m["x"] = 0;
    fork {
	   worker w1 {
	     int x = 10;
	     x -> w2;
	     int a = 0;
	     a <- w3;
	     (a * 2) -> w2;
	   }
	   worker w2 {
	     int a = 0;
	     a <- w1;
	     (a * 2) -> w3;
	     a <- w1;
	     m["x"] = a;
	     (a * 2) -> w3;
	     system:sleep(1000);
	     m["x"] = 33;
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     (a * 2) -> w1;
	     m["x"] <- w2;
	   }
	} join (all w2, w3) (map results) {  system:println(results);  }
	return m;
}

function forkJoinWithAllSelectedJoin2() (map) {
    map m = {};
    m["x"] = 0;
    fork {
	   worker w1 {
	     int x = 10;
	     x -> w2;
	     int a = 0;
	     a <- w3;
	     (a * 2) -> w2;
	   }
	   worker w2 {
	     int a = 0;
	     a <- w1;
	     (a * 2) -> w3;
	     a <- w1;
	     m["x"] = a;
	     (a * 2) -> w3;
	     system:sleep(2000);
	     m["x"] = 33;
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     (a * 2) -> w1;
	     m["x"] <- w2;
	   }
	} join (all w2, w3) (map results) { } timeout (1) (map results) {  m["x"] = 777;  }
	return m;
}



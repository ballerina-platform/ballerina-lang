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
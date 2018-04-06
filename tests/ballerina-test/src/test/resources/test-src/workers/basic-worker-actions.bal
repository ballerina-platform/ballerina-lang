import ballerina/io;
import ballerina/runtime;
function workerDeclTest() {
   worker wx {
     int a = 20;
     fork {
	   worker w1 {
	     int x = 0;
	   }
	   worker w2 {
	     int y = 0;
	     int g = y + 1;
	   }
	} join (all) (map results) { io:println(results); }
   }
   worker wy { }
}

function forkJoinWithMessageParsingTest() returns int {
    int x = 5;
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
	} join (all) (map results) { io:println(results); }
	return x;
}

function forkJoinWithSingleForkMessages() returns int {
    int x = 5;
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     a -> w2;
	     b <- w2;
	     a -> fork;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     a <- w1;
	     b -> w1;
	     b -> fork;
	   }
	} join (all) (map results) { io:println(results); }
	return x;
}

function basicForkJoinTest() returns int {
    int x = 10;
    fork {
	   worker w1 {
	     int a = 5;
	     int b = a + 1;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	   }
	} join (all) (map results) { }
	return x;
}

function forkJoinWithMultipleForkMessages() returns int {
    int x = 5;
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     a -> w2;
	     b <- w2;
		 (a, b) -> fork;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     a <- w1;
	     b -> w1;
		 (a, b) -> fork;
	   }
	} join (all) (map results) {  io:println(results);  }
	return x;
}

function simpleWorkerMessagePassingTest() {
   worker w1 {
     int a = 10;
     a -> w2;
     a <- w2;
   }
   worker w2 {
     int a = 0;
     int b = 15;
     a <- w1;
     b -> w1;
   }
}

function forkJoinWithSomeJoin() returns int | error {
    map m;
    m["x"] = 25;
    int ret;
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     m["x"] = a;
         runtime:sleepCurrentWorker(1000);
	   }
	   worker w2 {
	     int a = 5;
	     int b = 15;
         runtime:sleepCurrentWorker(1000);
	     m["x"] = a;
	   }
	   worker w3 {
	     int a = 0;
	     int b = 15;
	     m["x"] = b;
	   }
	} join (some 1) (map results) {  io:println(results);  }
	match <int> m["x"] {
	    int a => return a;
	    error err => return err;
	}
}

function workerReturnTest() returns int {
    worker wx {
	    int x = 50;
	    return x + 1;
    }
}

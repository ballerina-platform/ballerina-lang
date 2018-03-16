import ballerina.io;
import ballerina.net.http;
import ballerina.runtime;

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
	     runtime:sleepCurrentWorker(5000);
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
	     runtime:sleepCurrentWorker(100);
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
	     runtime:sleepCurrentWorker(2000);
	     m["x"] = a;
	   }
	   worker w3 {
	     int a = 0;
	     int b = 15;
         runtime:sleepCurrentWorker(1000);
	     m["x"] = b;
	   }
	} join (some 1 w2, w3) (map results) {  io:println(results);  }
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
	} join (some 1 w1, w2, w3) (map results) {  io:println(results);  }
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
	     runtime:sleepCurrentWorker(1000);
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     (a * 2) -> w1;
	     m["x"] <- w2;
	   }
	} join (some 1 w2, w3) (map results) {  io:println(results);  }
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
	     runtime:sleepCurrentWorker(1000);
	     m["x"] = a * 2;
	   }
	} join (some 2 w1, w2, w3) (map results) {  io:println(results);  }
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
	     runtime:sleepCurrentWorker(5000);
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
	     runtime:sleepCurrentWorker(1000);
	     m["x"] = 33;
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     (a * 2) -> w1;
	     m["x"] <- w2;
	   }
	} join (all w2, w3) (map results) {  io:println(results);  }
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
	     runtime:sleepCurrentWorker(2000);
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

function forkJoinWithMessagePassingTimeoutNotTriggered() (map) {
    map m = {};
    fork {
       worker w1 {
         int a = 5;
         a -> w2;
         int b = 0;
         b <- w2;
         b -> fork;
       }
       worker w2 {
         int a = 0;
         a <- w1;
         int b = 15;
         b -> w1;
         a -> fork;
       }
    } join (all) (map results) {
        any[] anyArray;
        int b;
        anyArray, _ = (any[]) results["w1"];
        b, _ = (int) anyArray[0];
        int a;
        anyArray, _ = (any[]) results["w2"];
        a, _ = (int) anyArray[0];
        m["x"] = (a + 1) * b;
    } timeout (5) (map results) { 
        m["x"] = 15; 
    }
    return m;
}

function forkJoinInWorkers() (int) {
    worker wx {
	    int x = 20;
	    map m = {};
	    fork {
		   worker w1 {
		     m["a"] = 10;
		   }
		   worker w2 {
		     m["b"] = 20;
		   }
	    } join (all) (map results) { 
	       int a;
	       int b;
	       a, _ = (int) m["a"];
	       b, _ = (int) m["b"];
	       x = a + b;
	    }
	    return x;
    }
}

function largeForkJoinCreationTest() (int) {
    int result = 0;
    map m = {};
    int c = 1000;
    while (c > 0) {
	    m["x"] = 10;
	    fork {
		   worker w1 {
		     int a = 2;
		     int b = 0;
		     a -> w2;
		     b <- w10;
		     m["x"] = result + b;
		   }
		   worker w2 {
		     int a = 0;
		     int b = 3;
		     a <- w1;
		     (a + b) -> w3;
		   }
		   worker w3 {
		     int a = 0;
		     int b = 4;
		     a <- w2;
		     (a + b) -> w4;
		   }
		   worker w4 {
		     int a = 0;
		     int b = 5;
		     a <- w3;
		     (a + b) -> w5;
		   }
		   worker w5 {
		     int a = 0;
		     int b = 6;
		     a <- w4;
		     (a + b) -> w6;
		   }
		   worker w6 {
		     int a = 0;
		     int b = 7;
		     a <- w5;
		     (a + b) -> w7;
		   }
		   worker w7 {
		     int a = 0;
		     int b = 8;
		     a <- w6;
		     (a + b) -> w8;
		   }
		   worker w8 {
		     int a = 0;
		     int b = 9;
		     a <- w7;
		     (a + b) -> w9;
		   }
		   worker w9 {
		     int a = 0;
		     int b = 10;
		     a <- w8;
		     (a + b) -> w10;
		   }
		   worker w10 {
		     int a = 0;
		     int b = 11;
		     a <- w9;
		     (a + b) -> w1;
		   }
	    } join (all) (map results) {
	       var x, _ = (int) m["x"]; 
	       result = x;
	    }
	    c = c - 1;
    }
    return result;
}

function forkJoinWithStruct () (string result) {
    fork {
        worker w1 {
            foo f = {x:1, y:"w1"};
            io:println(f);
            f -> fork;
        }
        worker w2 {
            float f = 10.344;
            io:println("[w2] f: " + f);
            f -> fork;
        }
    } join (all) (map results) {
        var resW1, _ = (any[])results["w1"];
        var f, _ = (foo)resW1[0];
        result = "[join-block] sW1: " + f.y;
        var resW2, _ = (any[])results["w2"];
        var fW2, _ = (float)resW2[0];
        result = result + "[join-block] fW2: " + fW2;
    }
    return result;
}

struct foo {
    int x;
    string y;
}

function forkJoinWithSameWorkerContent () (string result) {
    fork {
        worker w1 {
            any[] a = [];
            a -> fork;
        }
        worker w2 {
            any[] b = [];
            b -> fork;
        }

    } join (all) (map results) {
        io:println(results);
    }
    fork {
        worker w1 {
            string[] a = ["data1"];
            a -> fork;
        }
        worker w2 {
            string[] a = ["data2"];
            a -> fork;
        }
    } join (all) (map results) {
        var resW1, _ = (any[])results["w1"];
        var s1, _ = (string[])resW1[0];
        result = "W1: " + s1[0];
        var resW2, _ = (any[])results["w2"];
        var s2, _ = (string[])resW2[0];
        result = result + ", W2: " + s2[0];
    }
    return result;
}

//function testWorkerStackCreation ()(int) {
//    endpoint<http:HttpClient> c {
//        create http:HttpClient("http://example.com", {port:80,keepAlive:true, httpVersion : "1.1", ssl : {}});
//    }
//    worker w1 {
//        return 1;
//    }
//    worker w2 {
//        io:println("testWorkerStackCreation done.");
//    }
//}

import ballerina/runtime;

function forkJoinWithTimeoutTest1() returns map {
    map m;
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

function forkJoinWithTimeoutTest2() returns map {
    map m;
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

function complexForkJoinWorkerSendReceive() returns map {
    map m;
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

function chainedWorkerSendReceive() returns map {
    map m;
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

function forkJoinWithSomeSelectedJoin1() returns int {
    map m;
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
	} join (some 1 w2, w3) (map results) {  }
	int j;
	int k;
	j = check <int> m["x"];
	k = check <int> m["y"];
	return j * k;
}

function forkJoinWithSomeSelectedJoin2() returns map {
    map m;
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
	} join (some 1 w1, w2, w3) (map results) { }
	return m;
}

function forkJoinWithSomeSelectedJoin3() returns map {
    map m;
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
	} join (some 1 w2, w3) (map results) {  }
	return m;
}

function forkJoinWithSomeSelectedJoin4() returns int {
    map m;
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
	} join (some 2 w1, w2, w3) (map results) {  }
	int x = check <int> m["x"];
	return x;
}

function forkJoinWithSomeSelectedJoin5() returns int {
    map m;
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
	int x;
	x = check <int> m["x"];
	return x;
}

function forkJoinWithAllSelectedJoin1() returns map {
    map m;
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
	} join (all w2, w3) (map results) {  }
	return m;
}

function forkJoinWithAllSelectedJoin2() returns int {
    int result;
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
	     result = a;
	     (a * 2) -> w3;
	     runtime:sleepCurrentWorker(2000);
	     result = 33;
	   }
	   worker w3 {
	     int a = 0;
	     a <- w2;
	     (a * 2) -> w1;
	     result <- w2;
	   }
	} join (all w2, w3) (map results) { } timeout (1) (map results) {
	     if (result != 33) {
	         result = 777; 
	     }  
	}
	return result;
}

function forkJoinWithMessagePassingTimeoutNotTriggered() returns map {
    map m;
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
        int b = check <int> results["w1"];
        int a = check <int> results["w2"];
        m["x"] = (a + 1) * b;
    } timeout (5) (map results) { 
        m["x"] = 15; 
    }
    return m;
}

function forkJoinInWorkers() returns int {
    worker wx {
	    int x = 20;
	    map m;
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
	       a = check <int> m["a"];
	       b = check <int> m["b"];
	       x = a + b;
	    }
	    return x;
    }
}

function largeForkJoinCreationTest() returns int {
    int result = 0;
    map m;
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
	       var x = check <int> m["x"];
	       result = x;
	    }
	    c = c - 1;
    }
    return result;
}

function forkJoinWithStruct () returns string {
    string result;
    fork {
        worker w1 {
            foo f = {x:1, y:"w1"};
            f -> fork;
        }
        worker w2 {
            float f = 10.344;
            f -> fork;
        }
    } join (all) (map results) {
        var f = check <foo> results["w1"];
        result = "[join-block] sW1: " + f.y;
        var fW2 = check <float> results["w2"];
        result = result + "[join-block] fW2: " + fW2;
    }
    return result;
}

type foo {
    int x;
    string y;
};

function forkJoinWithSameWorkerContent () returns string {
    string result;
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
        string[] resW1 = check <string[]> results["w1"];
        var s1 = resW1[0];
        result = "W1: " + s1;
        string[] resW2 = check <string[]> results["w2"];
        var s2 = resW2[0];
        result = result + ", W2: " + s2;
    }
    return result;
}


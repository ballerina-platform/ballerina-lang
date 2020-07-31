import ballerina/io;
import ballerina/runtime;
import ballerina/java;

function workerDeclTest() {
     int a = 20;
     fork {
       @strand{thread:"any"}
	   worker w1 {
	     int x = 0;
	   }
	   @strand{thread:"any"}
	   worker w2 {
	     int y = 0;
	     int g = y + 1;
	   }
	}
    map<any> results = wait {w1, w2};
    io:println(results);
   worker wy { }
}

function forkWithMessageParsingTest() returns int {
    int x = 5;
    fork {
       @strand{thread:"any"}
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     a -> w2;
	     b = <- w2;
	   }
	   @strand{thread:"any"}
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     a = <- w1;
	     b -> w1;
	   }
	}
    map<any> results = wait {w1, w2};
    io:println(results);
	return x;
}

function basicForkTest() returns int {
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
	}
    map<any> results = wait {w1, w2};
	return x;
}

function simpleWorkerMessagePassingTest() {
   worker w1 {
     int a = 10;
     a -> w2;
     a = <- w2;
   }
   worker w2 {
     int a = 0;
     int b = 15;
     a = <- w1;
     b -> w1;
   }
}

function forkWithWaitForAny() returns int | error {
    map<any> m = {};
    m["x"] = 25;
    int ret;
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     m["x"] = a;
         runtime:sleep(1000);
	   }
	   worker w2 {
	     int a = 5;
	     int b = 15;
         runtime:sleep(1000);
	     m["x"] = a;
	   }
	   worker w3 {
	     int a = 0;
	     int b = 15;
	     m["x"] = b;
	   }
	}

    () results = wait w1 | w2 | w3;
    io:println(results);
    return <int>m["x"];
}

function workerReturnTest() returns int {
    worker wx returns int {
	    int x = 50;
	    return x + 1;
    }
    return (wait wx);
}

function workerSameThreadTest() returns any {
    worker w1 returns string {
        return getCurrentThreadName();
    }

    worker w2 returns string {
        return getCurrentThreadName();
    }
    map<string> res = wait {w1, w2};
    res["w"] = getCurrentThreadName();

    return res;
}

function getCurrentThreadName() returns string {
    handle t = currentThread();
    handle tName = getName(t);
    return java:toString(tName) ?: "";
}

function currentThread() returns handle = @java:Method {
    'class: "java.lang.Thread"
} external;

function getName(handle thread) returns handle = @java:Method {
    'class: "java.lang.Thread"
} external;

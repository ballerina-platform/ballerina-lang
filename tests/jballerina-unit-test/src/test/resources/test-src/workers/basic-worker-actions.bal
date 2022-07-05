import ballerina/jballerina.java;
import ballerina/lang.'value as value;

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
         sleep(1000);
	   }
	   worker w2 {
	     int a = 5;
	     int b = 15;
         sleep(1000);
	     m["x"] = a;
	   }
	   worker w3 {
	     int a = 0;
	     int b = 15;
	     m["x"] = b;
	   }
	}

    () results = wait w1 | w2 | w3;
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

function testSimpleSendActionWithCloneableType() {
    worker w1Cloneable returns boolean {
        value:Cloneable w1a = 10;
        value:Cloneable w1b = 11;
        w1a -> w2Cloneable;
        w1b ->> w2Cloneable;
        return true;
    }

    worker w2Cloneable returns boolean {
        value:Cloneable w2a = 15;
        value:Cloneable w2b = 15;
        sleep(10);
        w2a = <- w1Cloneable;
        w2b = <- w1Cloneable;
        if (w2a is int) {
            assertValueEquality(10, w2a);
        } else {
            return false;
        }
        if (w2b is int) {
            assertValueEquality(11, w2b);
        } else {
            return false;
        }
        return true;
    }

    record { boolean w1Cloneable; boolean w2Cloneable; } x = wait {w1Cloneable, w2Cloneable};
    assertValueEquality(true, x.w1Cloneable);
    assertValueEquality(true, x.w2Cloneable);
}

function testSimpleSendActionErrorType() {
    @strand {thread: "parent"}
    worker w1Error returns error {
        error w1a = error("10");
        error w1b = error("11");
        w1a -> w2Error;
        w1b ->> w2Error;
        return error("Completed");
    }

    @strand {thread: "parent"}
    worker w2Error returns error {
        error w2a = error("15");
        error w2b = error("15");
        sleep(10);
        w2a = <- w1Error;
        w2b = <- w1Error;
        assertValueEquality("10", w2a.message());
        assertValueEquality("11", w2b.message());
        return error("Completed");
    }

    record {
        error w1Error;
        error w2Error;
    } x = wait {w1Error, w2Error};
    assertValueEquality("Completed", x.w1Error.message());
    assertValueEquality("Completed", x.w2Error.message());
}

function testSimpleSendActionXMLType() {
    @strand {thread: "parent"}
    worker w1Xml returns xml {
        xml w1a = xml `10`;
        xml w1b = xml `11`;
        w1a -> w2Xml;
        w1b ->> w2Xml;
        return xml `Completed`;
    }

    @strand {thread: "parent"}
    worker w2Xml returns xml {
        xml w2a = xml `boom`;
        xml w2b = xml `boom`;
        sleep(10);
        w2a = <- w1Xml;
        w2b = <- w1Xml;
        assertValueEquality(xml `10`, w2a);
        assertValueEquality(xml `11`, w2b);
        return xml `Completed`;
    }

    record {
        xml w1Xml;
        xml w2Xml;
    } x = wait {w1Xml, w2Xml};
    assertValueEquality("Completed", x.w1Xml.toString());
    assertValueEquality("Completed", x.w2Xml.toString());
}

type IntRec readonly & record {
                           int ID = 0;
                       };

function testSimpleSendActionReadonlyRecord() {
    @strand {thread: "parent"}
    worker w1readonlyRec returns string {
        IntRec w1a = {ID: 10};
        IntRec w1b = {ID: 11};
        w1a -> w2readonlyRec;
        w1b ->> w2readonlyRec;
        return "Completed";
    }

    @strand {thread: "parent"}
    worker w2readonlyRec returns string {
        IntRec w2a = {ID: 3};
        IntRec w2b;
        sleep(10);
        w2a = <- w1readonlyRec;
        w2b = <- w1readonlyRec;
        assertValueEquality(10, w2a.ID);
        assertValueEquality(11, w2b.ID);
        return "Completed";
    }

    record {
        string w1readonlyRec;
        string w2readonlyRec;
    } x = wait {w1readonlyRec, w2readonlyRec};
    assertValueEquality("Completed", x.w1readonlyRec);
    assertValueEquality("Completed", x.w2readonlyRec);
}

function simpleSendActionWithMapType() returns map<anydata> {
    @strand {thread: "parent"}
    worker w1 returns boolean {
        map<int> x = { "a" : 1, "b" : 2};
        map<int> y = { "c" : 40, "d" : 50};
        map<value:Cloneable> a = x;
        map<value:Cloneable> b = y;
        a -> w2;
        b ->> w2;
        return true;
    }

    @strand {thread: "parent"}
    worker w2 returns boolean {
        map<value:Cloneable> data = <- w1;
        sleep(10);
        map<value:Cloneable> dataSet2 = <- w1;
        if (data is map<int>) {
            map<int> intData = <map<int>>data;
            assertValueEquality(1, intData["a"]);
            assertValueEquality(2, intData["b"]);
        } else {
            return false;
        }
        if (dataSet2 is map<int>) {
            map<int> intData = <map<int>>dataSet2;
            assertValueEquality(40, intData["c"]);
            assertValueEquality(50, intData["d"]);
        } else {
            return false;
        }
        return true;
    }

    @strand {thread: "any"}
    worker w3 returns map<anydata> {
        map<anydata> results = wait {w1, w2};
        return results;
    }

    map<anydata> waitedResult =  wait w3;
    return waitedResult;
}

function testSimpleSendActionWithMapType() {
    map<anydata> simpleSendActionWithMapTypeResult = simpleSendActionWithMapType();
    assertValueEquality(true, simpleSendActionWithMapTypeResult["w1"]);
    assertValueEquality(true, simpleSendActionWithMapTypeResult["w2"]);
}

function simpleSendActionWithListType() returns map<anydata> {
    @strand {thread: "parent"}
    worker w1 returns boolean {
        int[] x = [1, 2, 3];
        int[2] y = [40, 50];
        value:Cloneable[] a = x;
        value:Cloneable[2] b = y;
        a -> w2;
        b ->> w2;
        return true;
    }

    @strand {thread: "parent"}
    worker w2 returns boolean {
        value:Cloneable[] data = <- w1;
        sleep(10);
        value:Cloneable[] dataSet2 = <- w1;
        if (data is int[]) {
            int[] intData = <int[]>data;
            assertValueEquality(1, intData[0]);
            assertValueEquality(2, intData[1]);
            assertValueEquality(3, intData[2]);
        } else {
            return false;
        }
        if (dataSet2 is int[]) {
            int[] intData = <int[]>dataSet2;
            assertValueEquality(40, intData[0]);
            assertValueEquality(50, intData[1]);
        } else {
            return false;
        }
        return true;
    }

    @strand {thread: "any"}
    worker w3 returns map<anydata> {
        map<anydata> results = wait {w1, w2};
        return results;
    }

    map<anydata> waitedResult =  wait w3;
    return waitedResult;
}

function testSimpleSendActionWithListType() {
    map<anydata> simpleSendActionWithMapTypeResult = simpleSendActionWithListType();
    assertValueEquality(true, simpleSendActionWithMapTypeResult["w1"]);
    assertValueEquality(true, simpleSendActionWithMapTypeResult["w2"]);
}

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

function getCurrentThreadName() returns string {
    handle t = currentThread();
    handle tName = getName(t);
    return java:toString(tName) ?: "";
}

function basicWorkerTest1() returns error? {
    string username = "";

    if username == "" {
        return;
    }

    int x = 1;

    worker w {
        int _ = x;
    }

    function _ = function () {
        int _ = x;
    };
}

function basicWorkerTest2() returns error? {
    string? username = "ABC";

    if username is () {
        return;
    }

    string x = username;

    worker w {
        string y = x;
    }

    function _ = function () {
        string z = x;
    };

    string _ = x;
}

function currentThread() returns handle = @java:Method {
    'class: "java.lang.Thread"
} external;

function getName(handle thread) returns handle = @java:Method {
    'class: "java.lang.Thread"
} external;

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

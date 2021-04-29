import ballerina/jballerina.java;
import ballerina/lang.'value as value;

string append = "";
function simpleSyncSend() returns string {
    string done = process();
    return append;
}

function process() returns string {
    @strand{thread:"any"}
    worker w1 {
        int a = 10;
        a -> w2;
        a ->> w2;
        a -> w2;
        foreach var i in 1 ... 5 {
            append = append + "w1";
        }
    }

    @strand{thread:"any"}
    worker w2 {
        int b = 15;
        sleep(10);
        foreach var i in 1 ... 5 {
            append = append + "w2";
        }
        b = <- w1;
        b = <- w1;
        b = <- w1;
    }

    wait w1;
    return "done";
}

string append2 = "";
function multipleSyncSend() returns string {
    @strand{thread:"any"}
    worker w1 {
        int a = 10;
        var result = a ->> w2;
        error? res = result;
        foreach var i in 1 ... 5 {
            append2 = append2 + "w1";
        }
        result = a ->> w2;
        foreach var i in 1 ... 5 {
            append2 = append2 + "w11";
        }
    }

    @strand{thread:"any"}
    worker w2 returns error? {
        int b = 15;
        sleep(10);
        foreach var i in 1 ... 5 {
            append2 = append2 + "w2";
        }
        if (false) {
            error err = error("err", message = "err msg");
            return err;
        }
        b = <- w1;
        foreach var i in 1 ... 5 {
            append2 = append2 + "w22";
        }
        b = <- w1;
        return;
    }
    wait w1;
    return append2;
}

function process2() returns any|error {
    return returnNil();
}

function returnNil() returns any|error {
    @strand{thread:"any"}
    worker w1 returns any|error {
        int a = 10;
        a -> w2;
        var result = a ->> w2;
        foreach var i in 1 ... 5 {
            append = append + "w1";
        }
        return result;
    }

    @strand{thread:"any"}
    worker w2 returns error? {
        if (false) {
            error err = error("err", message = "err msg");
            return err;
        }
        int b = 15;
        sleep(10);
        foreach var i in 1 ... 5 {
            append = append + "w2";
        }
        b = <- w1;
        b = <- w1;
        return;
    }

    var result = wait w1;
    return result;
}

string append3 = "";
function multiWorkerSend() returns string {
    @strand{thread:"any"}
    worker w1 {
        int a = 10;
        var result = a ->> w2;
        error? res = result;
        a -> w3;
        foreach var i in 1 ... 5 {
            append3 = append3 + "w1";
        }
        result = a ->> w2;
        a -> w3;
        foreach var i in 1 ... 5 {
            append3 = append3 + "w11";
        }
    }

    @strand{thread:"any"}
    worker w2 returns error? {
        if (false) {
            error err = error("err", message = "err msg");
            return err;
        }

        if (false) {
            error err = error("err", message = "err msg");
            return err;
        }
        int b = 15;
        sleep(10);
        foreach var i in 1 ... 5 {
            append3 = append3 + "w2";
        }
        b -> w3;
        b = <- w1;
        b -> w3;
        foreach var i in 1 ... 5 {
            append3 = append3 + "w22";
        }
        b = <- w1;
        return;
    }


    @strand{thread:"any"}
    worker w3 returns error? {
        int|error x = <- w2;
        int b;
        foreach var i in 1 ... 5 {
            append3 = append3 + "w3";
        }
        b = <- w1;
        x = <- w2;
        foreach var i in 1 ... 5 {
            append3 = append3 + "w33";
        }
        b = <- w1;
        return;
    }

    wait w1;
    return append3;
}

string append4 = "";
function errorResult() returns error? {
    var inner = function () returns error? {
        @strand{thread:"any"}
        worker w1 returns error? {
            int a = 10;
            var result = a ->> w2;
            result = a ->> w3;
            foreach var i in 1 ... 5 {
                append4 = append4 + "w1";
            }
            result = a ->> w2;
            result = a ->> w3;
            foreach var i in 1 ... 5 {
                append3 = append4 + "w11";
            }

            return result;
        }

        @strand{thread:"any"}
        worker w2 returns error? {
            if (false) {
                error err = error("err", message = "err msg");
                return err;
            }
            int b = 15;
            sleep(10);
            foreach var i in 1 ... 5 {
                append4 = append4 + "w2";
            }
            b -> w3;
            b = <- w1;
            var result = b ->> w3;
            error? res = result;
            foreach var i in 1 ... 5 {
                append4 = append4 + "w22";
            }
            b = <- w1;
            return;
        }

        @strand{thread:"any"}
        worker w3 returns error|string {
            if (false) {
                error err = error("err", message = "err msg");
                return err;
            }
            int b;
            int|error be;
            be = <- w2;
            foreach var i in 1 ... 5 {
                append4 = append4 + "w3";
            }
            b = <- w1;

            int|error res = <- w2;
            if (res is int) {
                b = res;
            }

            if (b > 0) {
                map<string> reason = {k1: "error3"};
                error er3 = error(reason.get("k1"), message = "msg3");
                return er3;
            }
            foreach var i in 1 ... 5 {
                append4 = append4 + "w33";
            }
            b = <- w1;
            return "success";
        }

        error? w1Result = wait w1;
        return w1Result;
    };
    validateError(inner(), "error3");
}


function panicTest() returns error? {
    @strand{thread:"any"}
    worker w1 returns error? {
        int a = 10;
        a -> w2;
        var result = a ->> w3;

        a -> w2;
        result = a ->> w3;


        return result;
    }

    @strand{thread:"any"}
    worker w2 {
        int b = 15;
        sleep(10);

        b -> w3;
        b = <- w1;
        var result = b ->> w3;
        error? res = result;
        b = <- w1;
    }

    @strand{thread:"any"}
    worker w3 returns string|error {
        if (false) {
            error err = error("err", message = "err msg");
            return err;
        }
        int b;
        b = <- w2;

        b = <- w1;
        b = <- w2;
        if (b > 0) {
            map<string> reason = {k1: "error3"};
            error er3 = error(reason.get("k1"), message = "msg3");
            panic er3;
        }

        b = <- w1;
        return "success";
    }

    error? w1Result = wait w1;
    return w1Result;
}

function basicSyncSendTest() returns int {
    @strand{thread:"any"}
    worker w1 {
        int a = 10;
        int b = 20;
        () r1 = a ->> w2;
        () r2 = b ->> w2;
        a + b -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns int {
        int c = 0;
        c = <- w1;
        c += c;
        c = <- w1;
        c += c;
        c = <- w1;
        c += c;
        return c;
    }
    return wait w2;
}

function multipleSyncSendWithPanic() returns int {
    @strand{thread:"any"}
    worker w1 returns int {
        int a = 10;
        () r1 = a ->> w2;
        () r2 = a + 10 ->> w2;
        if (true) {
            error err = error("err from panic from w1");
            panic err;
        }
        return a;
    }

    @strand{thread:"any"}
    worker w2 returns error? {
        int b = 0;
        if (true) {
            error err = error("err from panic from w2");
            panic err;
        }
        b = <- w1;
        b = <- w1;
        return;
    }
    return wait w1;
}

function multipleSyncSendWithPanicInSend() returns int {
    @strand{thread:"any"}
    worker w1 returns int {
        int a = 10;
        () r1 = a ->> w2;
        if (true) {
            error err = error("err from panic from w1 w1");
            panic err;
        }
        () r2 = a + 10 ->> w2;
        if (true) {
            error err = error("err from panic from w1 w1");
            panic err;
        }
        return a;
    }

    @strand{thread:"any"}
    worker w2 {
        int b = 0;
        b = <- w1;
        b = <- w1;
    }
    return wait w1;
}

function syncSendWithPanicInReceive() {
    @strand{thread:"any"}
    worker w1 {
        int a = 10;
        () r1 = a ->> w2;
        () r2 = a + a ->> w2;
    }

    @strand{thread:"any"}
    worker w2 {
        int b1 = <- w1;
        if (true) {
            error err = error("err from panic from w2");
            panic err;
        }
        int b2 = <- w1;
    }
    wait w1;
}

function panicWithMultipleSendStmtsTest() returns error? {
    @strand{thread:"any"}
    worker w1 returns error? {
        int a = 10;
        a + 5 -> w2;
        () result = a + 10 ->> w3;
        a + 15 -> w2;
        result = a + 20 ->> w3;
        return result;
    }

    @strand{thread:"any"}
    worker w2 {
        int b1 = <- w1;
        b1 -> w3;
        int b2 = <- w1;
        () result = b2 ->> w3;
        sleep(1000);
    }

    @strand{thread:"any"}
    worker w3 returns string {
        int b = <- w1;
        if (2 != 2) {
            error err = error("err returned from w3");
            panic err;
        }
        b = <- w2;
        b += b;
        b = <- w2;

        if (b > 10) {
            error err = error("err from panic from w3w3");
            panic err;
        }

        b = <- w1;
        return "w3 received all send statments";
    }

    error? res = wait w1;
    return res;
}

function errorResultWithMultipleWorkers() {
    var inner = function () returns error? {
        @strand{thread:"any"}
        worker w1 returns error? {
            int x = 30;
            () n = x ->> w2;
            error? res = x ->> w2;
            return res;
        }

        @strand{thread:"any"}
        worker w2 returns int|error {
            int x = 0;
            x = <- w1;
            if (true) {
                error err = error("err returned from w2");            // Already errored
                return err;
            }
            int res = <- w1;
            return res;
        }

        error? eor = wait w1;
        return eor;
    };

}

public type Rec record {
    int k = 0;
};

public function testComplexType() returns Rec {
    @strand{thread:"any"}
    worker w1 {
        Rec rec = {};
        rec.k = 10;
        //int i = 40;
        () sendRet = rec ->> w2;
        rec.k = 50;
        sendRet = 5 ->> w2;
    }

    @strand{thread:"any"}
    worker w2 returns Rec {
        int l = 25;
        Rec j = {};
        j = <- w1;
        l = <- w1;
        return j;
    }

    return wait w2;
}

public function multipleSendsToErroredChannel() {
    var inner = function () returns error? {
        @strand{thread:"any"}
        worker w1 returns error? {
            error? a = 5 ->> w2;

            error? b = "foo" ->> w2;

            return b;
        }

        @strand{thread:"any"}
        worker w2 returns error? {
            boolean b = true;
            if (b) {
                error e1 = error("error one");
                return e1;
            }

            int x = <- w1;

            if (!b) {
                error e2 = error("error two");
                return e2;
            }

            string y = <- w1;
        }

        error? res = wait w1;
        return res;
    };

    validateError(inner(), "error one");
}

public function testSyncSendAfterSend() returns error? {
    var inner = function () returns error? {
        @strand{thread:"any"}
        worker w1 returns error? {
            5 -> w2;

            error? x = "foo" ->> w2;
            return x;
        }

        @strand{thread:"any"}
        worker w2 returns error? {
            if (true) {
                error e = error("w2 error");
                return e;
            }

            int x = <- w1;
            string s = <- w1;
        }

        return wait w1;
    };

    validateError(inner(), "w2 error");
}

const R1 = "r1";
const R2 = "r2";

type E1 distinct error;
type E2 distinct error;

public function testNoFailureForReceiveWithError() returns boolean {
    @strand{thread:"any"}
    worker w1 returns boolean|E1|E2? {
        if (getFalse()) {
            return error E1(R1);
        }
        100 ->> w2;

        if (getFalse()) {
            return error E2(R2);
        }
        error? err = "hello" ->> w2;
        return err is ();
    }

    @strand{thread:"any"}
    worker w2 returns boolean|error? {
        int|E1 v1 = <- w1;

        if (getFalse()) {
            return error("w2 err");
        }
        string|E1|E2 v2 = <- w1;
        return v1 === 100 && v2 === "hello";
    }

    record { boolean|E1|E2? w1; boolean|error? w2; } x = wait { w1, w2 };
    return x.w1 === true && x.w2 === true;
}

public function testFailureForReceiveWithError() returns boolean {
    worker w1 returns boolean|E1|E2? {
        if (getFalse()) {
            return error E1(R1);
        }
        100 ->> w2;

        if (getTrue()) {
            return error E2(R2);
        }
        error? err = "hello" ->> w2;
        return err is ();
    }

    @strand{thread:"any"}
    worker w2 returns boolean|error? {
        int|E1 v1 = <- w1;

        if (getFalse()) {
            return error("w2 err");
        }
        string|E1|E2 v2 = <- w1;
        return v1 === 100 && v2 is E2;
    }

    record { boolean|E1|E2? w1; boolean|error? w2; } x = wait { w1, w2 };
    return x.w1 is E2 && x.w2 === true;
}

string appendCloneable = "";
function testSimpleSyncSendWithCloneableExpression() {
    string done = processWithCloneableExpression();
    assertValueEquality("done", done);
    assertValueEquality("w2cw2cw2cw2cw2cw1cw1cw1cw1cw1c", appendCloneable);
}

function processWithCloneableExpression() returns string {
    worker w1Cloneable returns boolean {
        value:Cloneable w1a = 10;
        value:Cloneable w1b = 11;
        value:Cloneable w1c = 12;
        w1a -> w2Cloneable;
        w1b ->> w2Cloneable;
        w1c -> w2Cloneable;
        foreach var i in 1 ... 5 {
            appendCloneable = appendCloneable + "w1c";
        }
        return true;
    }

    worker w2Cloneable returns boolean {
        value:Cloneable w2a = 15;
        value:Cloneable w2b = 15;
        value:Cloneable w2c = 15;
        sleep(10);
        foreach var i in 1 ... 5 {
            appendCloneable = appendCloneable + "w2c";
        }
        w2a = <- w1Cloneable;
        w2b = <- w1Cloneable;
        w2c = <- w1Cloneable;
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
        if (w2c is int) {
            assertValueEquality(12, w2c);
        } else {
            return false;
        }
        return true;
    }

    record { boolean w1Cloneable; boolean w2Cloneable; } x = wait {w1Cloneable, w2Cloneable};
    assertValueEquality(true, x.w1Cloneable);
    assertValueEquality(true, x.w2Cloneable);
    return "done";
}

function simpleSyncSendErrorType() returns string {
    @strand {thread: "parent"}
    worker w1Error returns error {
        error w1a = error("10");
        error w1b = error("11");
        error w1c = error("12");
        w1a -> w2Error;
        w1b ->> w2Error;
        w1c -> w2Error;
        foreach var i in 1 ... 5 {
            appendErrorMessages = appendErrorMessages + "w1e";
        }
        return error("Completed");
    }

    @strand {thread: "parent"}
    worker w2Error returns error {
        error w2a = error("15");
        error w2b = error("15");
        error w2c = error("15");
        sleep(10);
        foreach var i in 1 ... 5 {
            appendErrorMessages = appendErrorMessages + "w2e";
        }
        w2a = <- w1Error;
        w2b = <- w1Error;
        w2c = <- w1Error;
        assertValueEquality("10", w2a.message());
        assertValueEquality("11", w2b.message());
        assertValueEquality("12", w2c.message());
        return error("Completed");
    }

    record {
        error w1Error;
        error w2Error;
    } x = wait {w1Error, w2Error};
    assertValueEquality("Completed", x.w1Error.message());
    assertValueEquality("Completed", x.w2Error.message());
    return "done";
}

function simpleSyncSendXMLType() returns string {
    @strand {thread: "parent"}
    worker w1Xml returns xml {
        xml w1a = xml `10`;
        xml w1b = xml `11`;
        xml w1c = xml `12`;
        w1a -> w2Xml;
        w1b ->> w2Xml;
        w1c -> w2Xml;
        foreach var i in 1 ... 2 {
            xmlValue = xmlValue.concat(w1a);
        }
        return xml `Completed`;
    }

    @strand {thread: "parent"}
    worker w2Xml returns xml {
        xml w2a = xml `boom`;
        xml w2b = xml `boom`;
        xml w2c = xml `boom`;
        sleep(10);
        foreach var i in 1 ... 2 {
            xmlValue = xmlValue.concat(w2a);
        }
        w2a = <- w1Xml;
        w2b = <- w1Xml;
        w2c = <- w1Xml;
        assertValueEquality(xml `10`, w2a);
        assertValueEquality(xml `11`, w2b);
        assertValueEquality(xml `12`, w2c);
        return xml `Completed`;
    }

    record {
        xml w1Xml;
        xml w2Xml;
    } x = wait {w1Xml, w2Xml};
    assertValueEquality("Completed", x.w1Xml.toString());
    assertValueEquality("Completed", x.w2Xml.toString());
    return "done";
}

type IntRec readonly & record {
                           int ID = 0;
                       };

string readonlyRecIDs = "START";

function testSimpleSyncSendWithReadonlyRecord() {
    assertValueEquality("done", simpleSyncSendReadonllyRecord());
    assertValueEquality("START101033", readonlyRecIDs.toString());
}

xml xmlValue = xml ``;
function testSimpleSyncSendWithXMLType() {
    assertValueEquality("done", simpleSyncSendXMLType());
    assertValueEquality("boomboom1010", xmlValue.toString());
}

string appendErrorMessages = "";
function testSimpleSyncSendWithErrorType() {
    assertValueEquality("done", simpleSyncSendErrorType());
    assertValueEquality("w2ew2ew2ew2ew2ew1ew1ew1ew1ew1e", appendErrorMessages);
}

function simpleSyncSendReadonllyRecord() returns string {
    @strand {thread: "parent"}
    worker w1readonlyRec returns string {
        IntRec w1a = {ID: 10};
        IntRec w1b = {ID: 11};
        IntRec w1c = {ID: 12};
        foreach var i in 1 ... 2 {
            readonlyRecIDs = readonlyRecIDs.concat(w1a.ID.toString());
        }
        w1a -> w2readonlyRec;
        w1b ->> w2readonlyRec;
        w1c -> w2readonlyRec;
        return "Completed";
    }

    @strand {thread: "parent"}
    worker w2readonlyRec returns string {
        IntRec w2a = {ID: 3};
        IntRec w2b;
        IntRec w2c;
        sleep(10);
        foreach var i in 1 ... 2 {
            readonlyRecIDs = readonlyRecIDs.concat(w2a.ID.toString());
        }
        w2a = <- w1readonlyRec;
        w2b = <- w1readonlyRec;
        w2c = <- w1readonlyRec;
        assertValueEquality(10, w2a.ID);
        assertValueEquality(11, w2b.ID);
        assertValueEquality(12, w2c.ID);
        return "Completed";
    }

    record {
        string w1readonlyRec;
        string w2readonlyRec;
    } x = wait {w1readonlyRec, w2readonlyRec};
    assertValueEquality("Completed", x.w1readonlyRec);
    assertValueEquality("Completed", x.w2readonlyRec);
    return "done";
}

function getFalse() returns boolean {
    return false;
}

function getTrue() returns boolean {
    return true;
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

function validateError(any|error value, string message) {
    if (value is error) {
        if (value.message() == message) {
            return;
        }
        panic error("Expected error message: " + message + ", found: " + value.message());
    }
    panic error("Expected error, found: " + (typeof value).toString());
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

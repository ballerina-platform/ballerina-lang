import ballerina/runtime;


string append = "";
function simpleSyncSend() returns string {
    string done = process();
    return append;
}

function process() returns string {
    worker w1 {
        int a = 10;
        a -> w2;
        a ->> w2;
        a -> w2;
        foreach var i in 1 ... 5 {
            append = append + "w1";
        }
    }

    worker w2 {
        int b = 15;
        runtime:sleep(10);
        foreach var i in 1 ... 5 {
            append = append + "w2";
        }
        b = <- w1;
        b = <- w1;
        b = <- w1;
    }

    wait w1;
    //runtime:sleep(50);
    return "done";
}

string append2 = "";
function multipleSyncSend() returns string {
    worker w1 {
        int a = 10;
        var result = a ->> w2;
        foreach var i in 1 ... 5 {
            append2 = append2 + "w1";
        }
        result = a ->> w2;
        foreach var i in 1 ... 5 {
            append2 = append2 + "w11";
        }
    }

    worker w2 returns error? {
        int b = 15;
        runtime:sleep(10);
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
    worker w1 returns any|error {
        int a = 10;
        a -> w2;
        var result = a ->> w2;
        foreach var i in 1 ... 5 {
            append = append + "w1";
        }
        return result;
    }

    worker w2 returns error? {
        if (false) {
            error err = error("err", message = "err msg");
            return err;
        }
        int b = 15;
        runtime:sleep(10);
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
    worker w1 {
        int a = 10;
        var result = a ->> w2;
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
        runtime:sleep(10);
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

    worker w2 returns error? {
        if (false) {
            error err = error("err", message = "err msg");
            return err;
        }
        int b = 15;
        runtime:sleep(10);
        foreach var i in 1 ... 5 {
            append4 = append4 + "w2";
        }
        b -> w3;
        b = <- w1;
        var result = b ->> w3;
        foreach var i in 1 ... 5 {
            append4 = append4 + "w22";
        }
        b = <- w1;
        return;
    }

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
}


function panicTest() returns error? {
    worker w1 returns error? {
        int a = 10;
        a -> w2;
        var result = a ->> w3;

        a -> w2;
        result = a ->> w3;


        return result;
    }

    worker w2 {
        int b = 15;
        runtime:sleep(10);

        b -> w3;
        b = <- w1;
        var result = b ->> w3;

        b = <- w1;
    }

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
    worker w1 {
        int a = 10;
        int b = 20;
        () r1 = a ->> w2;
        () r2 = b ->> w2;
        a + b -> w2;
    }

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

    worker w2 {
        int b = 0;
        b = <- w1;
        b = <- w1;
    }
    return wait w1;
}

function syncSendWithPanicInReceive() {
    worker w1 {
        int a = 10;
        () r1 = a ->> w2;
        () r2 = a + a ->> w2;
    }

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
    worker w1 returns error? {
        int a = 10;
        a + 5 -> w2;
        () result = a + 10 ->> w3;
        a + 15 -> w2;
        result = a + 20 ->> w3;
        return result;
    }

    worker w2 {
        int b1 = <- w1;
        b1 -> w3;
        int b2 = <- w1;
        () result = b2 ->> w3;
        runtime:sleep(1000);
    }

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

function errorResultWithMultipleWorkers() returns error? {
    worker w1 returns error? {
        int x = 30;
        () n = x ->> w2;
        error? res = x ->> w2;
        return res;
    }

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
}

public type Rec record {
    int k = 0;
};

public function testComplexType() returns Rec {
    worker w1 {
        Rec rec = {};
        rec.k = 10;
        //int i = 40;
        () sendRet = rec ->> w2;
        rec.k = 50;
        sendRet = 5 ->> w2;
    }

    worker w2 returns Rec {
        int l = 25;
        Rec j = {};
        j = <- w1;
        l = <- w1;
        return j;
    }

    return wait w2;
}

public function multipleSendsToErroredChannel() returns error? {
    worker w1 returns error? {
        error? a = 5 ->> w2;

        error? b = "foo" ->> w2;

        return b;
    }

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
}

public function testSyncSendAfterSend() returns error? {
    worker w1 returns error? {
        5 -> w2;

        error? x = "foo" ->> w2;
        return x;
    }

    worker w2 returns error? {
        if (true) {
            error e = error("w2 error");
            return e;
        }

        int x = <- w1;
        string s = <- w1;
    }

    return wait w1;
}

const R1 = "r1";
const R2 = "r2";

type E1 error<R1>;
type E2 error<R2>;

public function testNoFailureForReceiveWithError() returns boolean {
    worker w1 returns boolean|E1|E2? {
        if (getFalse()) {
            return E1();
        }
        100 ->> w2;

        if (getFalse()) {
            return E2();
        }
        error? err = "hello" ->> w2;
        return err is ();
    }

    worker w2 returns boolean|error? {
        int|E1 v1 = <- w1;

        if (getFalse()) {
            return error("w2 err");
        }
        string|E1|E2 v2 = <- w1;
        return v1 == 100 && v2 == "hello";
    }

    record { boolean|E1|E2? w1; boolean|error? w2; } x = wait { w1, w2 };
    return x.w1 == true && x.w2 == true;
}

public function testFailureForReceiveWithError() returns boolean {
    worker w1 returns boolean|E1|E2? {
        if (getFalse()) {
            return E1();
        }
        100 ->> w2;

        if (getTrue()) {
            return E2();
        }
        error? err = "hello" ->> w2;
        return err is ();
    }

    worker w2 returns boolean|error? {
        int|E1 v1 = <- w1;

        if (getFalse()) {
            return error("w2 err");
        }
        string|E1|E2 v2 = <- w1;
        return v1 == 100 && v2 is E2;
    }

    record { boolean|E1|E2? w1; boolean|error? w2; } x = wait { w1, w2 };
    return x.w1 is E2 && x.w2 == true;
}

function getFalse() returns boolean {
    return false;
}

function getTrue() returns boolean {
    return true;
}

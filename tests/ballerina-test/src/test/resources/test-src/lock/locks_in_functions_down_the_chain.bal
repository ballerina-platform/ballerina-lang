import ballerina/runtime;

string lockString = "";

function lockWithinLock() returns string {
    worker w1 {
        lock {
            lockString = lockString + "w1w1";
            lockLevel2("w1v");
            runtime:sleep(100);
        }
    }
    worker w2 {
        lock {
            runtime:sleep(200);
            lockString = lockString + "w2w2";
            lockLevel2("w2v");
            return lockString;
        }
    }
}

function lockLevel2(string s) {
    lockLevel3(s);
}

function lockLevel3(string s) {
    lock {
        lockString = lockString + s;
    }
}

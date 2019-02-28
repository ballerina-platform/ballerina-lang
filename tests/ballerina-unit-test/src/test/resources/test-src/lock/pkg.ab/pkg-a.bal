
int lockInt1 = 0;

string lockString1 = "";

public function updateLockInt1(int value) {
    lockInt1 = value;
}

public function updateLockString1(string value) {
    lockString1 = value;
}

public function getLockInt1() returns int {
    lock {
        return lockInt1;
    }
}

public function getLockString1() returns string {
    lock {
        return lockString1;
    }
}

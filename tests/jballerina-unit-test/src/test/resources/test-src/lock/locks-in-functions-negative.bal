int intLockValue = 0;

function testInvalidScopeInLock () {
    lock {
        int val = 99;
        intLockValue = intLockValue + 50;
    }
    val = 88;
}

function testInvalidScopeInComplexLock () {
    lock {
        int val = 99;
        intLockValue = intLockValue + 50;
        lock {
            int val1 = val + 44;
        }
        val1 = 88;
    }
}
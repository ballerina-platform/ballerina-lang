isolated map<string>|int[] val1 = [];
isolated int[]|int val2 = [];
isolated map<string>|int[]|boolean val3 = [];

function fn1() returns map<string>|int[] {
    lock {
        return val1;
    }
}

function fn2() returns int[]|int {
    lock {
        return val2;
    }
}

function fn3() returns map<string>|int[]|boolean {
    lock {
        return val3;
    }
}

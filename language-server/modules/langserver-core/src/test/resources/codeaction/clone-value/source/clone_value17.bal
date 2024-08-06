isolated map<string>|int[] val1 = [];
isolated int[]|int val2 = [];
isolated map<string>|int[]|boolean val3 = [];

function fn1(map<string>|int[] a) {
    lock {
        val1 = a;
    }
}

function fn2(int[]|int a) {
    lock {
        val2 = a;
    }
}

function fn3(map<string>|int[]|boolean a) {
    lock {
        val3 = a;
    }
}

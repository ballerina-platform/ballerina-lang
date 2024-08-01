isolated map<string> mp1 = {};
isolated map<map<map<string>>> mp2 = {};
isolated map<map<int[]>> mp3 = {};

function fn1(map<string> a) {
    lock {
        mp1 = a;
    }
}

function fn2(map<string> a) {
    lock {
        mp2["a"]["a"] = a;
    }
}

function fn3(int[] a) {
    lock {
        mp3["a"]["b"] = a;
    }
}

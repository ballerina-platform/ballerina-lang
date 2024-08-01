isolated map<string> mp1 = {};
isolated map<string|int> mp2 = {};
isolated map<map<string>> mp3 = {};
isolated map<map<map<map<string[]>>>> mp4 = {};

isolated function fn1(map<string> a) {
    lock {
        mp1 = a;
    }
}

isolated function fn2(map<string|int> a) {
    lock {
        mp2 = a;
    }
}

isolated function fn3(map<map<string>> a) {
    lock {
        mp3 = a;
    }
}

isolated function fn4(map<string> a) {
    lock {
        mp3["a"] = a;
    }
}

isolated function fn5(map<string[]> a) {
    lock {
        mp4["a"]["b"]["c"] = a;
    }
}

isolated function fn6(string[] a) {
    lock {
        mp4["a"]["b"]["c"]["d"] = a;
    }
}

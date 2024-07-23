isolated map<string> mp1 = {};
isolated map<string|int> mp2 = {};
isolated map<map<string>> mp3 = {};
isolated map<map<map<map<string[]>>>> mp4 = {};

isolated function fn1() returns map<string> {
    lock {
        return mp1;
    }
}

isolated function fn2() returns map<string|int> {
    lock {
        return mp2;
    }
}

isolated function fn3() returns map<map<string>> {
    lock {
        return mp3;
    }
}

isolated function fn4() returns map<string> {
    lock {
        return mp3.get("a");
    }
}

isolated function fn5() returns map<string[]> {
    lock {
        return mp4.get("a").get("b").get("c");
    }
}

isolated function fn6() returns string[] {
    lock {
        return mp4.get("a").get("b").get("c").get("d");
    }
}

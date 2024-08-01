isolated map<string> mp1 = {};
isolated map<map<map<string>>> mp2 = {};
isolated map<map<int[]>> mp3 = {};

function fn1() returns map<string> {
    lock {
        return mp1;
    }   
}

function fn2() returns map<string> {
    lock {
        return mp2.get("a").get("b");
    }   
}

function fn3() returns int[] {
    lock {
        return mp3.get("a").get("b");
    }   
}

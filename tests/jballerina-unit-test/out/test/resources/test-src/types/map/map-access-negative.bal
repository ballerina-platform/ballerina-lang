function incorrectMapAccessTest() returns (string?) {
    map<string> animals = {};
    animals["dog"] = "Jimmy";
    return animals["cat"];
}

function accessUninitMap() {
    map<int> ints;
    ints["0"] = 0;

    map<int> m1 = getUninitializedMap11(ints);

    map<int> m2 = getUninitializedMap21();
}


function getUninitializedMap11(map<int> m) returns map<int> {
    map<int> m2 = getUninitializedMap12(m);
    return m2;
}

function getUninitializedMap12(map<int> m) returns map<int> {
    map<int> m2 = m;
    return m2;
}

function getUninitializedMap21() returns map<int> {
    map<int> m3 = getUninitializedMap22();
    return m3;
}

function getUninitializedMap22() returns map<int> {
    map<int> m4;
    return m4;
}

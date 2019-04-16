function incorrectMapAccessTest() returns (string?) {
    map<any> animals = {};
    animals["dog"] = "Jimmy";
    return animals[0];
}

function accessAllFields() {
    map<any> fruits = {"name":"John", "address":"unkown"};
    any a = fruits.*;
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

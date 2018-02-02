function checkGreaterThanForUnsupportedType () (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 > j2;
}

function checkGreaterThanEualForUnsupportedType () (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 >= j2;
}


function checkLessThanForUnsupportedType () (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 < j2;
}

function checkLessThanEqualForUnsupportedType () (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 <= j2;
}

function checkGreaterThan () (boolean) {
    int a;
    string b;
    return a > b;
}

function checkGreaterThanEual () (boolean) {
    int a;
    string b;
    return a >= b;
}

function checkLessThan () (boolean) {
    int a;
    string b;
    return a < b;
}

function checkLessThanEqual () (boolean) {
    int a;
    string b;
    return a <= b;
}

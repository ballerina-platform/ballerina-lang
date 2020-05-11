function checkGreaterThanForUnsupportedType () returns (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 > j2;
}

function checkGreaterThanEualForUnsupportedType () returns (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 >= j2;
}


function checkLessThanForUnsupportedType () returns (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 < j2;
}

function checkLessThanEqualForUnsupportedType () returns (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 <= j2;
}

function checkGreaterThan () returns (boolean) {
    int a = 0;
    string b = "";
    return a > b;
}

function checkGreaterThanEual () returns (boolean) {
    int a = 0;
    string b = "";
    return a >= b;
}

function checkLessThan () returns (boolean) {
    int a = 0;
    string b = "";
    return a < b;
}

function checkLessThanEqual () returns (boolean) {
    int a = 0;
    string b = "";
    return a <= b;
}

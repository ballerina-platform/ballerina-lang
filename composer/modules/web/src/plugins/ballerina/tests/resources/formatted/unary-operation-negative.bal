function testJsonPositive () (json) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j1 = +j2;

    return j1;
}

function testJsonNegative () (json) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j1 = -j2;

    return j1;
}

function testJsonNot () (json) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j1 = !j2;

    return j1;
}
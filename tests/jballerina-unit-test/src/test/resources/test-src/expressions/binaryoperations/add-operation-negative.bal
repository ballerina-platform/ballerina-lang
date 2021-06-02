function addJson() {
    json j1;
    json j2;
    json j3;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};
    // Following line is invalid.
    j3 = j1 + j2;
}

function addIncompatibleTypes() {
    int i;
    // Following line is invalid.
    i = 5 + "abc";
}

const A = 10;
const B = 20;

type C A|B;

function addIncompatibleTypes2() {
    C a = 10;
    string b = "ABC";
    float|int c = 12;
    xml e = xml `abc`;

    int i1 = a + b;
    int i2 = a + c;
    xml i3 = a + e;
}

const E = "ABC";

type D A|E;

type F "ABC"|"CDE";

type G "ABC"|10;

function addIncompatibleTypes3() {
    D a = 10;
    int b = 12;
    F c = "ABC";
    G d = 10;

    int i1 = a + b;
    int i2 = c + b;
    int i3 = d + b;
}

function testImplicitConversion() {
    float a = 1;
    decimal b = 1;
    int c = 1;
    var x1 = a + b;
    any x2 = a + b;
    var x3 = a + c;
    any x4 = b + c;
    anydata x5 = c + a;

    C d = 10;
    float e = 12.25;
    var y1 = d + e;
    any y2 = d + e;
}

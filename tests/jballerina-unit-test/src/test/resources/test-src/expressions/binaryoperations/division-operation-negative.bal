function divideJson () {
    json j1;
    json j2;
    json j3;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};
    // Following line is invalid.
    j3 = j1 / j2;
}

function divideIncompatibleTypes () {
    string str;
    // Following line is invalid.
    str = "foo" / 3.0;
}

const A = 10;
const B = 20;

type C A|B;

function divisionIncompatibleTypes() {
    C a = 10;
    string b = "ABC";
    float|int c = 12;
    string|string:Char e = "D";

    int i1 = a / b;
    int i2 = a / c;
    string i3 = b / e;
}

function testImplicitConversion() {
    float a = 1;
    decimal b = 1;
    int c = 1;
    var x1 = a / b;
    any x2 = a / b;
    anydata x5 = c / a;
    anydata x6 = c / b;

    C d = 10;
    float e = 12.25;
    var y1 = d / e;
    any y2 = d / e;
}

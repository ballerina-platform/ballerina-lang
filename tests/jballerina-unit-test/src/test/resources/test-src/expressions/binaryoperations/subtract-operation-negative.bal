function incompatibleSubtract () {
    float f;
    // Following line is invalid.
    f = 5 - "foo";
}

function sunstractJson () {
    json j1;
    json j2;
    json j3;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};
    // Following line is invalid.
    j3 = j1 - j2;
}

const A = 10;
const B = 20;

type C A|B;

function subtractIncompatibleTypes() {
    C a = 10;
    string b = "ABC";
    float|int c = 12;
    string|string:Char e = "D";

    int i1 = a - b;
    int i2 = a - c;
    string i3 = b - e;
}

function testImplicitConversion() {
    float a = 1;
    decimal b = 1;
    var i1 = a - b;

    C d = 10;
    float e = 12.25;
    var i2 = d - e;
}

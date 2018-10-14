const name = "Ballerina";

const age = 10;

public const id = 123;

function getName() returns string {
    return name;
}

function getAge() returns int {
    return age;
}

function getId() returns int {
    return id;
}

function concatConstants() returns string {
    return "Hello " + name;
}

// -------------------------------------

function typeTest() returns int {
    HttpRequest req = "POST";
    int value = validate(req);
    return value;
}

function validate(HttpRequest req) returns int {
    if (req == GET){
        return 1;
    } else if (req == POST) {
        return 2;
    }
    return 0;
}

type HttpRequest "GET"|"POST";

const GET = "GET";
const POST = "POST";

// -------------------------------------

function testConstantsWithoutTypes() returns int {
    AB b = B;
    int value = validateAB(b);
    return value;
}

function validateAB(AB ab) returns int {
    if (ab == A){
        return 2;
    } else if (ab == B) {
        return 4;
    }
    return 0;
}

type AB A|B;

const A = "a";
const string B = "b";

// -------------------------------------

function testConstantsWithTypes() returns int {
    CD c = C;
    int value = validateCD(c);
    return value;
}

function validateCD(CD cd) returns int {
    if (cd == C){
        return 3;
    } else if (cd == D) {
        return 5;
    }
    return 0;
}

type CD C|D;

const C = "c";
const string D = "d";

// -------------------------------------

function testMixedWithLiteral() returns int {
    EFG g = "G";
    int value = validateEFG(g);
    return value;
}

function validateEFG(EFG efg) returns int {
    if (efg == E){
        return 3;
    } else if (efg == F) {
        return 5;
    } else if (efg == "G") {
        return 7;
    }
    return 0;
}

type EFG E|F|"G";

const E = "e";
const string F = "f";

// -------------------------------------

function testMixedWithObject() returns int {

    K k = new;

    HIJ value1 = k.getHIJ(true);
    var value2 = k.getHIJ(false);

    return 0;
}

type K object {

    function getHIJ(boolean b) returns HIJ {
        if (b) {
            var h = new H("Ballerina");
            return h;
        }
        return I;
    }

};

type HIJ H|I|J;

type H object {
    string s;

    public new(s) {}
};

const I = "i";

const string J = "j";

// -------------------------------------

function testMixedWithFinal() returns int {
    L l = 10;
    return 0;
}

type L M|N;

final var M = "m";

type N int;

// -------------------------------------

function checkTypeAsParam() returns int {
    return checkValue(STRING);
}

function checkValue(ValueType vType) returns int {
    if (vType == STRING) {
        return 1;
    }
    return 2;
}

type ValueType "STRING"|"INT";

const ValueType STRING = "STRING";
const ValueType INT = "INT";

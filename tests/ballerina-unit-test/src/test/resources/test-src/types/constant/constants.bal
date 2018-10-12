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

const GET = "GET";
const POST = "POST";

type HttpRequest "GET"|"POST";

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

// -------------------------------------

const A = "a";
const B = "b";

type AB A|B;

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

// -------------------------------------

const string C = "c";
const string D = "d";

type CD C|D;

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

// -------------------------------------

const E = "e";
const string F = "f";

type EFG E|F|"G";

function testMixed() returns int {
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
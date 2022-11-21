import testorg/foo as test_project;

// -----------------------------------------------------------

type Ballerina "Ballerina";

function testAccessConstantWithoutType() returns Ballerina {
    return test_project:constName;
}

function testAccessConstantWithoutTypeAsString() returns string {
    return test_project:constName;
}

type Colombo "Colombo";

function testAccessConstantWithType() returns Colombo {
    return test_project:constAddress;
}

// -----------------------------------------------------------

type AB "A"|"B";

function testAccessFiniteType() returns test_project:AB {
    return test_project:getA();
}

function testReturnFiniteType() returns AB {
    return test_project:getA(); // Valid because this is same as `return "A";`
}

function testAccessTypeWithContInDef() returns test_project:XY {
    test_project:XY x = "X";
    return x;
}

// -----------------------------------------------------------

test_project:XY test = "X";

function testTypeFromAnotherPackage() returns test_project:XY {
    return test;
}

// -----------------------------------------------------------

type M record { string f; }|Z;

M m1 = { f: "test_project" };

M m2 = "V";

M m3 = "W";

M m4 = "X";

type Y X;

type Z "V"|W|X;

Y y = "X";

Z z1 = "V";

Z z2 = "W";

Z z3 = "X";


const string W = "W";

const string X = "X";

// -----------------------------------------------------------

type BooleanTypeWithType test_project:booleanWithType;

function testBooleanTypeWithType() returns BooleanTypeWithType {
    BooleanTypeWithType t = false;
    return t;
}

const booleanWithoutType = true;

type BooleanTypeWithoutType booleanWithoutType;

function testBooleanTypeWithoutType() returns BooleanTypeWithoutType {
    BooleanTypeWithoutType t = true;
    return t;
}

// -----------------------------------------------------------

type IntTypeWithType test_project:intWithType;

function testIntTypeWithType() returns IntTypeWithType {
    IntTypeWithType t = 40;
    return t;
}


type IntTypeWithoutType test_project:intWithoutType;

function testIntTypeWithoutType() returns IntTypeWithoutType {
    IntTypeWithoutType t = 20;
    return t;
}

// -----------------------------------------------------------

type ByteTypeWithType test_project:byteWithType;

function testByteTypeWithType() returns ByteTypeWithType {
    ByteTypeWithType t = 240;
    return t;
}

// -----------------------------------------------------------

type FloatTypeWithType test_project:floatWithType;

function testFloatTypeWithType() returns FloatTypeWithType {
    FloatTypeWithType t = 4.0;
    return t;
}

type FloatTypeWithoutType test_project:floatWithoutType;

function testFloatTypeWithoutType() returns FloatTypeWithoutType {
    FloatTypeWithoutType t = 2.0;
    return t;
}

// -----------------------------------------------------------

type DecimalTypeWithType test_project:decimalWithType;

function testDecimalTypeWithType() returns DecimalTypeWithType {
    DecimalTypeWithType t = 50.0;
    return t;
}

// -----------------------------------------------------------

type StringTypeWithType test_project:stringWithType;

function testStringTypeWithType() returns StringTypeWithType {
    StringTypeWithType t = "Ballerina is awesome";
    return t;
}

type StringTypeWithoutType test_project:stringWithoutType;

function testStringTypeWithoutType() returns StringTypeWithoutType {
    StringTypeWithoutType t = "Ballerina rocks";
    return t;
}

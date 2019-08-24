import testorg/foo;

// -----------------------------------------------------------

type Ballerina "Ballerina";

function testAccessConstantWithoutType() returns Ballerina {
    return foo:constName;
}

function testAccessConstantWithoutTypeAsString() returns string {
    return foo:constName;
}

type Colombo "Colombo";

function testAccessConstantWithType() returns Colombo {
    return foo:constAddress;
}

// -----------------------------------------------------------

type AB "A"|"B";

function testAccessFiniteType() returns foo:AB {
    return foo:getA();
}

function testReturnFiniteType() returns AB {
    return foo:getA(); // Valid because this is same as `return "A";`
}

function testAccessTypeWithContInDef() returns foo:XY {
    foo:XY x = "X";
    return x;
}

// -----------------------------------------------------------

foo:XY test = "X";

function testTypeFromAnotherPackage() returns foo:XY {
    return test;
}

// -----------------------------------------------------------

// Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/11183.
//type M record { string f; }|Z;
//
//M m1 = { f: "foo" };
//
//M m2 = "V";
//
//M m3 = "W";
//
//M m4 = "X";

type Y X;

type Z "V"|W|X;

Y y = "X";

Z z1 = "V";

Z z2 = "W";

Z z3 = "X";


const string W = "W";

const string X = "X";

// -----------------------------------------------------------

type BooleanTypeWithType foo:booleanWithType;

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

type IntTypeWithType foo:intWithType;

function testIntTypeWithType() returns IntTypeWithType {
    IntTypeWithType t = 40;
    return t;
}


type IntTypeWithoutType foo:intWithoutType;

function testIntTypeWithoutType() returns IntTypeWithoutType {
    IntTypeWithoutType t = 20;
    return t;
}

// -----------------------------------------------------------

type ByteTypeWithType foo:byteWithType;

function testByteTypeWithType() returns ByteTypeWithType {
    ByteTypeWithType t = 240;
    return t;
}

// -----------------------------------------------------------

type FloatTypeWithType foo:floatWithType;

function testFloatTypeWithType() returns FloatTypeWithType {
    FloatTypeWithType t = 4.0;
    return t;
}

type FloatTypeWithoutType foo:floatWithoutType;

function testFloatTypeWithoutType() returns FloatTypeWithoutType {
    FloatTypeWithoutType t = 2.0;
    return t;
}

// -----------------------------------------------------------

type DecimalTypeWithType foo:decimalWithType;

function testDecimalTypeWithType() returns DecimalTypeWithType {
    DecimalTypeWithType t = 50.0;
    return t;
}

// -----------------------------------------------------------

type StringTypeWithType foo:stringWithType;

function testStringTypeWithType() returns StringTypeWithType {
    StringTypeWithType t = "Ballerina is awesome";
    return t;
}

type StringTypeWithoutType foo:stringWithoutType;

function testStringTypeWithoutType() returns StringTypeWithoutType {
    StringTypeWithoutType t = "Ballerina rocks";
    return t;
}

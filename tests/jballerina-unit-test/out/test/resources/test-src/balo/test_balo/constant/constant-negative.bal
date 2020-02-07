import testorg/simple_literal;

// -----------------------------------------------------------

type BooleanTypeWithType simple_literal:booleanWithType;

function testBooleanTypeWithType() returns BooleanTypeWithType {
    BooleanTypeWithType t = true;
    return t;
}

type BooleanTypeWithoutType simple_literal:booleanWithoutType;

function testBooleanTypeWithoutType() returns BooleanTypeWithoutType {
    BooleanTypeWithoutType t = false;
    return t;
}

// -----------------------------------------------------------

type IntTypeWithType simple_literal:intWithType;

function testIntTypeWithType() returns IntTypeWithType {
    IntTypeWithType t = 100;
    return t;
}

type IntTypeWithoutType simple_literal:intWithoutType;

function testIntTypeWithoutType() returns IntTypeWithoutType {
    IntTypeWithoutType t = 100;
    return t;
}

// -----------------------------------------------------------

type ByteTypeWithType simple_literal:byteWithType;

function testByteTypeWithType() returns ByteTypeWithType {
    ByteTypeWithType t = 120;
    return t;
}

// -----------------------------------------------------------

type FloatTypeWithType simple_literal:floatWithType;

function testFloatTypeWithType() returns FloatTypeWithType {
    FloatTypeWithType t = 10.0;
    return t;
}

type FloatTypeWithoutType simple_literal:floatWithoutType;

function testFloatTypeWithoutType() returns FloatTypeWithoutType {
    FloatTypeWithoutType t = 10.0;
    return t;
}

// -----------------------------------------------------------

type DecimalTypeWithType simple_literal:decimalWithType;

function testDecimalTypeWithType() returns DecimalTypeWithType {
    DecimalTypeWithType t = 10.0;
    return t;
}

// -----------------------------------------------------------

type StringTypeWithType simple_literal:stringWithType;

function testStringTypeWithType() returns StringTypeWithType {
    StringTypeWithType t = "random text";
    return t;
}

type StringTypeWithoutType simple_literal:stringWithoutType;

function testStringTypeWithoutType() returns StringTypeWithoutType {
    StringTypeWithoutType t = "random text";
    return t;
}

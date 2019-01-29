import testorg/foo version v1;

// -----------------------------------------------------------

type BooleanTypeWithType foo:booleanWithType;

function testBooleanTypeWithType() returns BooleanTypeWithType {
    BooleanTypeWithType t = true;
    return t;
}

type BooleanTypeWithoutType foo:booleanWithoutType;

function testBooleanTypeWithoutType() returns BooleanTypeWithoutType {
    BooleanTypeWithoutType t = false;
    return t;
}

// -----------------------------------------------------------

type IntTypeWithType foo:intWithType;

function testIntTypeWithType() returns IntTypeWithType {
    IntTypeWithType t = 100;
    return t;
}

type IntTypeWithoutType foo:intWithoutType;

function testIntTypeWithoutType() returns IntTypeWithoutType {
    IntTypeWithoutType t = 100;
    return t;
}

// -----------------------------------------------------------

type ByteTypeWithType foo:byteWithType;

function testByteTypeWithType() returns ByteTypeWithType {
    ByteTypeWithType t = 120;
    return t;
}

// -----------------------------------------------------------

type FloatTypeWithType foo:floatWithType;

function testFloatTypeWithType() returns FloatTypeWithType {
    FloatTypeWithType t = 10.0;
    return t;
}

type FloatTypeWithoutType foo:floatWithoutType;

function testFloatTypeWithoutType() returns FloatTypeWithoutType {
    FloatTypeWithoutType t = 10.0;
    return t;
}

// -----------------------------------------------------------

type DecimalTypeWithType foo:decimalWithType;

function testDecimalTypeWithType() returns DecimalTypeWithType {
    DecimalTypeWithType t = 10.0;
    return t;
}

// -----------------------------------------------------------

type StringTypeWithType foo:stringWithType;

function testStringTypeWithType() returns StringTypeWithType {
    StringTypeWithType t = "random text";
    return t;
}

type StringTypeWithoutType foo:stringWithoutType;

function testStringTypeWithoutType() returns StringTypeWithoutType {
    StringTypeWithoutType t = "random text";
    return t;
}

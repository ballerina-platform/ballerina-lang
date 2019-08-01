
public const boolean booleanWithType = false;
public const booleanWithoutType = true;

public const int intWithType = 40;
public const intWithoutType = 20;

public const byte byteWithType = 240;

public const float floatWithType = 4.0;
public const floatWithoutType = 2.0;

public const decimal decimalWithType = 50.0;

public const string stringWithType = "Ballerina is awesome";
public const stringWithoutType = "Ballerina rocks";

// -----------------------------------------------------------

public const nameWithoutType = "Ballerina";
public const string nameWithType = "Ballerina";

// -----------------------------------------------------------

public const NilWithoutType = ();
public const () NilWithType = ();

// -----------------------------------------------------------

string sgvWithType = nameWithType;

public function getSgvWithType() returns string {
    return sgvWithType;
}

string sgvWithoutType = nameWithoutType;

public function getSgvWithoutType() returns string {
    return sgvWithoutType;
}

// -----------------------------------------------------------

public type ACTION "GET"|"POST";

public const GET = "GET";
public const POST = "POST";

public const constActionWithType = "GET";
public const constActionWithoutType = "GET";

// -----------------------------------------------------------

public const boolean conditionWithType = true;
public const conditionWithoutType = true;

// -----------------------------------------------------------

public type FiniteFloatType floatWithType|floatWithoutType;

// -----------------------------------------------------------

public const string KEY = "key";
public const string VALUE = "value";

// -----------------------------------------------------------

public const D = "D";

public const E = "E";

public const F = "F";

public type G D|E|F;

public type H D|E;

public const h = "D";

// -----------------------------------------------------------

public const string SHA1 = "SHA1";

public type TestRecord record {
    string field;
};

public type STRING_LABEL string;

public const STRING_LABEL labeledString = "Ballerina";

// -----------------------------------------------------------

public const aBoolean = true;
public const aInt = 24;
public const aByte = 12;
public const aFloat = 25.5;
public const decimal aDecimal = 25.5;

// -----------------------------------------------------------

public const constName = "Ballerina";

public const string constAddress = "Colombo";

public type AB "A"|"B";

AB A = "A";

public function getA() returns AB {
    return A;
}

// -----------------------------------------------------------

public type XY X|Y;

public const X = "X";

public const Y = "Y";

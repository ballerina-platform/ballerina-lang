function testUndeclaredStructAccess() {
    string name = "";
    dpt1[name] = "HR";
}

function testUndeclaredAttributeAccess() {
    string name;
    Department dpt = {};
    dpt["id"] = "HR";
}

type Department record {
    string dptName = "";
    int count = 0;
    !...;
};

function testInvalidTypeOfIndexExpression1() {
    Department dpt = {};
    int index = 1;
    var x = dpt[index]; // incompatible types: expected 'string', found 'int'
}

function testInvalidTypeOfIndexExpression2() {
    Department dpt = {};
    string index = "dptName";
    string x = dpt[index]; // incompatible types: expected 'string', found 'string|int?'
}

type FiniteOne "fieldOne"|"fieldTwo"|0;
type FiniteTwo 0|1;
type FiniteThree "fieldOne"|"fieldTwo"|"fieldThree";

type Foo record {
    string|boolean fieldOne;
    int fieldTwo;
    float fieldThree;
};

type Bar record {
    string|boolean fieldOne;
    int fieldTwo;
    !...;
};

function testFiniteTypeAsIndex() {
    FiniteOne f1 = "fieldOne";
    FiniteTwo f2 = 0;
    FiniteThree f3 = "fieldOne";

    Foo foo = { fieldOne: "S", fieldTwo: 12, fieldThree: 98.9 };
    Bar bar = { fieldOne: "S", fieldTwo: 12 };

    string|boolean|int|float? v1 = foo[f1]; // invalid index expression: invalid finite type value space 'fieldOne|fieldTwo|0'
    string|boolean|int|float? v2 = foo[f2]; // invalid index expression: invalid finite type value space '0|1'
    string|boolean|int|float? v3 = foo[f3];

    string|boolean|int|float? v4 = bar[f1]; // invalid index expression: invalid finite type value space 'fieldOne|fieldTwo|0'
    string|boolean|int|float? v5 = bar[f2]; // invalid index expression: invalid finite type value space '0|1'
    string|boolean|int|float? v6 = bar[f3]; // invalid index expression: invalid finite type value space 'fieldOne|fieldTwo|fieldThree'
}

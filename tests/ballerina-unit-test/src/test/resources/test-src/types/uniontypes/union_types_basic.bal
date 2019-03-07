import ballerina/io;

function testUnionTypeBasics1() returns (int|float|string, int|string) {
    int|float|string aaa = 12330;
    int|string bbb = "string value";

    aaa = 12.0;
    bbb = "sameera";

    return (aaa, bbb);
}

function testUnionTypeBasics2() returns (int|float|string|boolean) {
    int|float|string|boolean ttt = getUnion("jayasoma");
    return ttt;
}


function getUnion(string|int|float si) returns (int|float|string) {
    return "union types";
}


function testNullableTypeBasics1() returns (int|json|string|float|map<any>|boolean|()) {
    int|string|float|json|map<any>|boolean|() k = 5;

    k = "sss";
    k = 1.0;

    json j = { name: "ddd" };
    k = j;

    map<any> m = { name: "dddd" };
    k = m;

    k = true;

    k = ();
    return k;

}


function testNullableTypeBasics2() returns (int|boolean|()) {

    int|float|() x = ();

    if x is float|int {
        io:println("int");
    } else {
        io:println("null");
    }

    int|boolean|() i = ();

    if i is int {
        io:println("int");
    } else if i is boolean {
        io:println("boolean");
    } else {
        io:println(i);
    }

    return i;

}

public type ParamAny record {
    any value;
};

public type GlobalParam string|int|boolean|float|ParamAny;

function testUnionTypeArrays() returns int {
    ParamAny para1 = { value: "Colombo" };
    ParamAny para2 = { value: 10 };
    ParamAny[] paramAnyArray = [para1, para2];
    GlobalParam?[] globalParamArray = paramAnyArray;
    return globalParamArray.length();
}


function testUnionTypeArrayWithValueTypeArrayAssignment() returns int {
    int[] intArray = [10, 20, 30];
    GlobalParam?[] globalParamArray = intArray;
    return globalParamArray.length();
}

public type Person object {
    string name = "";
};

public type RecPerson record {
    string name;
    int id;
};

function testRecordLiteralAssignment() returns string {
    Person|RecPerson x = {name:"John", id:12};
    if x is Person {
        return "Invalid";
    } else {
        return <string> x.name;
    }
}

type Foo record {
    string s;
    int i = 0;
    !...;
};

type Bar record {
    string x;
    int y = 0;
    !...;
};

function testUnionTypeWithMultipleRecordTypes() returns string[] {

    string[] returnValues = [];

    Foo|Bar var1 = {s : "dummy string"};
    Foo|Bar var2 = {x : "dummy string"};

    if (var1 is Foo) {
        returnValues[0] = "FOO";
    } else {
        returnValues[0] = "BAR";
    }

    if (var2 is Foo) {
        returnValues[1] = "FOO";
    } else {
        returnValues[1] = "BAR";
    }

    return returnValues;
}

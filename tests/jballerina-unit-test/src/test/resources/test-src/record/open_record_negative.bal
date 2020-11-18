type Person record {|
    string name = "";
    int age = 0;
    string...;
|};

function invalidRestField() {
    Person p = { name: "John", age: 20, "height": 6, "employed": false, "city": "Colombo" };
}

type PersonA record {|
    string name = "";
    int age = 0;
    anydata|json...;
|};

function ambiguousEmptyRecordForRestField() {
    PersonA p = { name:"John", "misc": {} };
}

type Pet record {
    Animal lion;
};

class Bar {
    int a = 0;
}

function testInvalidRestFieldAddition() {
    PersonA p = {};
    p["invField"] = new Bar();
}

type Baz record {|
    int a;
    anydata...;
|};

type Qux record {
    string s;
};

type MyError error;

function testErrorAdditionForInvalidRestField() {
    error e1 = error("test reason");
    MyError e2 = error("test reason 2", err = e1);
    Baz b = { a: 1 };
    b["err1"] = e1;
    b["err2"] = e2;

    Qux q = { s: "hello" };
    q["e1"] = e1;
    q["e2"] = e2;
}

function testAnydataOrErrorRestFieldRHSAccess() {
    Person p = {};
    anydata|error name = p?.firstName;
}

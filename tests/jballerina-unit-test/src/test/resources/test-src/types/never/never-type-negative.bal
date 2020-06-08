function testNeverTypedVariable() {
    never x;
}

//------------ Testing a function with 'never' return type ---------

function functionWithNeverReturnType() returns never {
    string a = "hello";
}

function testAssignNeverReturnedFuncCall() {
    never x = functionWithNeverReturnType();
}

function testMisAssignNeverReturnedFuncCall() {
    () x = functionWithNeverReturnType();
}

function testAssignAnyToNeverReturnedFuncCall() {
    any x = functionWithNeverReturnType();
}

function testNeverReturnTypeWithStringReturnStmt1() returns never {
    string s = "hello";
    return s;
}

function testNeverReturnTypeWithStringReturnStmt2(boolean x) returns never {
    string s = "hello";
    if (x) {
        return s;
    }
}

function testNeverReturnTypeWithNilReturnStmt1() returns never {
    return ();
}

function testNeverReturnTypeWithNilReturnStmt2(boolean x) returns never {
        if (x) {
            return ();
        }
}

//------------ Testing record type with 'never' typed field ---------

type Foo record {
    int x;
    never y;
};

type Bar record {
    int x;
    never y?;
};

function testAssignValueToRequiredNeverField() {
    Foo foo = {x:2, y:"example"};
}

function testAssignValueToOptionalNeverField() {
    Foo foo = {x:2, y:"s"};
}

function testNeverFiledAssignNeverReturnedFunction() {
    Foo foo = {x:2, y:functionWithNeverReturnType()};
}

type Person record {|
    string name;
    never age?;
|};

function testAssignOptionalFieldWithAccessExpr1() {
    Person p = {name: "John"};
    p["age"] = 34;
}

function testAssignOptionalFieldWithAccessExpr2() {
    Person p = {name: "Kafka"};
    p["age"] = ();
}

type Vehicle record {|
    string brand;
    never...;
|};

function testAssignFieldWithNeverTypedSpreadOp1() {
    Vehicle p = {brand: "Mazda"};
    p["foo"] = "champ";
}

function testAssignFieldWithNeverTypedSpreadOp2() {
    Vehicle p = {brand: "Suzuki"};
    p["foo"] = ();
}

type Employee record {|
    string role;
    *Person;
|};

function testReferencingTypeWithNeverField() {
    Employee e = {name: "John", age: 20, role: "SE"};
}

//------------ Testing 'map' type with 'never' type ---------

function testAssignFieldsOfMappingWithNeverType1() {
    map<never> m;
    m["a"] = 122;
}

function testAssignFieldsOfMappingWithNeverType2() {
    map<never> m;
    m["a"] = ();
}

// -------------Test 'never' with table key constraints --------------
type Human record {
  readonly string name;
  int age;
};

type HumanTable table<Human> key<never>;

function testNeverAsTableKeyConstraint() {
    HumanTable humanTable= table key(name)[
        { name: "DD", age: 33},
        { name: "XX", age: 34}
    ];
}

type SomeHumanTable table<Human> key<never|string>;

function testNeverInUnionTypedKeyConstraints() {
    SomeHumanTable someHumanTable = table [
        { name: "MM", age: 33},
        { name: "PP", age: 34}
    ];
}

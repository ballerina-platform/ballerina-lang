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
    Bar bar = {x:2, y:"s"};
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

//------------ Testing a never type assignment ---------
function testNeverAssignment() {
     string empty = "";
     xml<never> c = xml `${empty}`;
     xml|'xml:Text d = xml ``;
     string e = d;
     xml|'xml:Text f = xml ``;
     string g = f;
     xml<never> h = xml ``;
     int|float i = h;
     string|'xml:Text p = xml ``;
     string s8 = p;
     int|string t = xml ``;
}

function testNeverTypeLocalVarDeclWithoutInit() {
    never a;
}

never b;

const never c = ();

function testNeverTypeInTypedBindingPattern() {
    never x = foo();
    var y = foo();
}

function foo() returns never {
  error e = error("Bad Sad!!");
  panic e;
}

function blow1(never rec) {
}

function blow2(record {| never x; |} rec) {
}

function blow3(never rec) {
}

function blow4(int val, record {| never x; |} rec = {}) {
}

function blow5() returns error? {
    Bam bam = new;
    bam->func();
    bam->func2();
    bam->func3();
}

client class Bam {
    remote function func() returns never {
        panic error("error!");
    }

    remote function func2() returns [never] {
        panic error("error!");
    }

    remote function func3() returns record {| never x; |} {
        panic error("error!");
    }
}

function testNeverTypeInTypedBindingPattern2() {
    [never] x = [];
}

function testNeverEquivalentRequiredArgInFunc1([never] a = foo()) {

}

function testNeverEquivalentRequiredArgInFunc2(record {| never x; |} a, object { never value; } b) {

}

type Record1 record {|
    int i;
    record {| never x; |} j;
|};

type Record2 record {|
    int i;
    record {| never x; |} j = {};
|};

function testNeverEquivRecord() {
    Record1 a = { i: 1 };
    Record2 b = { i: 1 };
}

type Pair record {
    int x;
};

function testNeverFieldTypeBinding() {
    Pair p = {
        x: 1,
        "y": 2,
        "color": "blue"
    };
    var {x: _, ...rest} = p;
    record {never x?; never y?;} y1 = rest;

    record {} a = {"x": 11};
    record {never x?;} y2 = a;
}

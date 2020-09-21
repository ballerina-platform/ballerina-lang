import ballerina/lang.'object as lang;

public function expressions() returns error? {
    //expression :=
    //   literal[!]
    //   | string-template-expr
    //   | xml-template-expr
    //   | raw-template-expr
    //   | structural-constructor-expr
    //   | new-expr
    //   | service-constructor-expr
    //   | variable-reference-expr
    //   | field-access-expr
    //   | optional-field-access-expr
    //   | xml-attribute-access-expr
    //   | annot-access-expr
    //   | member-access-expr
    //   | function-call-expr
    //   | method-call-expr
    //   | functional-constructor-expr
    //   | anonymous-function-expr
    //   | let-expr
    //   | type-cast-expr
    //   | typeof-expr
    //   | unary-expr
    //   | multiplicative-expr
    //   | additive-expr
    //   | shift-expr
    //   | range-expr
    //   | numerical-comparison-expr
    //   | is-expr
    //   | equality-expr
    //   | binary-bitwise-expr
    //   | logical-expr
    //   | conditional-expr
    //   | checking-expr
    //   | trap-expr
    //   | query-expr
    //   | xml-navigate-expr
    //   | ( expression )
    //
    // NOTE: [!] Not related to signatureHelp

    // -- expression, literal
    // "", 123,

    // -- expression, string-template-expr
    string s = string `${ foo(1, false) } !!!`;

    // -- expression, xml-template-expr
    xml x = xml `<foo>${ foo(1, false) }</foo>`;

    // -- expression, raw-template-expr
    var template = `Hello ${ foo(1, false) }!!!`;

    // -- expression, structural-constructor-expr
    // structural-constructor-expr := list-constructor-expr | table-constructor-expr | mapping-constructor-expr
    // list-constructor-expr
    int[] list = [foo(1, false)];
    // table-constructor-expr
    // table key(id) [
    //    { id: 1, name: "Mary", salary: 300 },
    //    { id: 2, name: "John", salary: 200 },
    //    { id: 3, name: "Jim", salary: foo(1, false) }
    // ];
    // mapping-constructor-expr
    map<int> map1 = {"3": foo(1, true)};
    map<int> map2 = {[fooS(1, false)]: 1};

    // -- expression, new-expr
    // new-expr := explicit-new-expr | implicit-new-expr
    Stub stub1 = new(2);
    Stub stub2 = new Stub(2);

    // -- expression, service-constructor-expr[!] refer end of this function

    // -- expression, variable-reference-expr
    int func = foo(1, false);

    // -- expression, field-access-expr
    Stub stub3 = new Stub(2);
    stub3.foo(1, false);
    stub3.bar(1, false).foo(1, false);
    stub3.obj.foo(1, false);

    // -- expression, optional-field-access-expr
    // optional-field-access-expr = expression ?. field-name
    Stub stub4 = new Stub(2);
    Annot? c = stub4.bar2(1, false)?.bar;

    // -- expression,  xml-attribute-access-expr
    (string|error)? attrs = fooX(1, false)?.attr;

    // -- expression, annot-access-expr
    // annot-access-expr := expression .@ annot-tag-reference
    Annot? annot = fooT(1, false).@v1;

    // -- expression,  member-access-expr
    string[] arr = [];
    string s1 = fooA(1, false)[0];
    string s2 = arr[foo(1, false)];

    // -- expression, function-call-expr
    int f1 = foo(1, false);
    int f2 = foo(fooF(1, false), false);
    int f3 = foo(a = fooF(1, false), b = false);
    int f4 = fooR(1, ...[foo(1, false)]);

    // -- expression, method-call-expr
    Stub stub5 = new Stub(2);
    stub5.obj.foo(1, false);

    // -- expression, functional-constructor-expr
    error err1 = error("");
    MyError err2 = MyError("");

    // -- expression, anonymous-function-expr
    function (float, boolean) returns int func1 =
            function (float a, boolean b) returns int {
                return foo(1, false);
            };

    // -- expression, let-expr
    int d = let int b = 1 in b * foo(1, false);

    // -- expression, type-cast-expr
    int f5 = <int>foo(1, false);

    // -- expression, typeof-expr
    typedesc<int> t2 = typeof foo(1, false);

    // -- expression, unary-expr
    int f6 = ~foo(1, false);
    int f7 = +foo(1, false);
    int f8 = -foo(1, false);
    boolean f9 = !fooB(1, false);

    // -- expression, multiplicative-expr
   int f10 = foo(1, false) * foo(1, false);
   int f11 = foo(1, false) / foo(1, false);
   int f12 = foo(1, false) % foo(1, false);

    // -- expression, additive-expr
   int f13 = foo(1, false) + foo(1, false);
   int f14 = foo(1, false) - foo(1, false);

    // -- expression, shift-expr
   int f15 = foo(1, false) >> foo(1, false);
   int f16 = foo(1, false) << foo(1, false);
   int f17 = foo(1, false) >>> foo(1, false);

    // -- expression, range-expr
    foreach var i in foo(1, false) ... foo(1, false) {
        // code
    }
    foreach var i in foo(1, false) ..< foo(1, false) {
        // code
    }

    // -- expression, numerical-comparison-expr
    boolean b1 = foo(1, false) < foo(1, false);
    boolean b2 = foo(1, false) > foo(1, false);
    boolean b3 = foo(1, false) <= foo(1, false);
    boolean b4 = foo(1, false) >= foo(1, false);

    // -- expression, is-expr
    boolean b5 = fooErr(1, false) is int;

    // -- expression, equality-expr
    boolean b6 = foo(1, false) == foo(1, false);
    boolean b7 = foo(1, false) != foo(1, false);
    boolean b8 = foo(1, false) === foo(1, false);
    boolean b9 = foo(1, false) !== foo(1, false);

    // -- expression, binary-bitwise-expr
    int f18 = foo(1, false) & foo(1, false);
    int f19 = foo(1, false) ^ foo(1, false);
    int f20 = foo(1, false) | foo(1, false);

    // -- expression, logical-expr
    boolean b10 = fooB(1, false) && fooB(1, false);
    boolean b11 = fooB(1, false) || fooB(1, false);

    // -- expression, conditional-expr
    int f21 = fooB(1, false)? foo(1, false) : foo(1, false);
    int f22 = fooO(1, false) ?: foo(1, false);

    // -- expression, checking-expr
    int|error aa = check fooErr(1, false);
    int bb = checkpanic fooErr(1, false);

    // -- expression, trap-expr
    int|error e1 = trap fooErr(1, false);

    // -- expression, query-expr
    // query-expr := query-pipeline select-clause
    // query-pipeline := from-clause intermediate-clause*
    // intermediate-clause := from-clause | where-clause | let-clause
    Child[] children = [];
    Child[] filtered1 = from var child in fooObjA(1, false) select new();
    Child[] filtered2 = from var child in children where fooB(1, false) select new();
    Child[] filtered3 = from var child in children let int newAge = foo(1, false) select new();

    // -- expression, xml-navigate-expr

    // -- expression, ( expression )
}

// -- service-constructor-expr
service negativeTemplateURI on ep {
    resource function newResource() {
        fooV(1, false);
    }
}

# Function Foo
# + a - a float
# + b - b bool
# + return - an integer
function foo(float a, boolean b) returns int {
    return 1;
}

# Function Foo
# + a - a float
# + b - b bool
# + return - a list of children
function fooObjA(float a, boolean b) returns Child[] {
    return [];
}

# Function Foo or Error
# + a - a float
# + b - b bool
# + return - an integer
function fooErr(float a, boolean b) returns (int | error) {
    return 1;
}

function fooS(float a, boolean b) returns string {
    return "get";
}

function fooT(float a, boolean b) returns typedesc<T1> {
    T1 c = { name: "John" };
    return typeof c;
}

function fooA(float a, boolean b) returns string[] {
    return [];
}

function fooX(float a, boolean b) returns xml {
    xml x = xml `<foo>${ foo(1,false) }</foo>`;
    return x;
}

function fooF(float a, boolean b) returns float {
    return 1;
}

function fooR(float a, int...b) returns int {
    return 1;
}

function fooB(float a, boolean b) returns boolean {
    return false;
}

function fooO(float a, boolean b) returns int? {
    return 1;
}

function fooE(float a, boolean b) returns error? {
    return error("");
}

function fooV(float a, boolean b) {

}

# Returns bar
# + a - float
# + b - boolean
# + return - child
public function bar2(float a, boolean b) returns Child {
    return new Child();
}

public class Child {
    public int age = 10;

    # Returns foo
    # + a - float
    # + b - boolean
    public function foo(float a, boolean b) {

    }
}

public client class Stub {

    public Child obj = new Child();

    # Create a new Stub
    public function init(any arg) {

    }

    # Returns name
    # + a - float
    # + b - boolean
    public function foo(float a, boolean b) {

    }

    # Returns bar
    # + a - float
    # + b - boolean
    # + return - child
    public function bar(float a, boolean b) returns Child {
        return new Child();
    }

    # Returns bar
    # + a - float
    # + b - boolean
    # + return - child
    public function bar2(float a, boolean b) returns Annot2? {
        return {foo: new Child()};
    }
}

public type Annot2 record {
    Child foo;
    Annot bar?;
};

public type Annot record {
    Child foo;
    Child bar?;
};

public annotation Annot v1 on type;
@v1 {
    foo: bar2(1, false),
    bar: bar2(1, false)
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testAnnotationAccessExpression() {
	typedesc<T1> t = typeof a;
    Annot? annotationVal = t.@v1;
    if (annotationVal is Annot) {
        annotationVal.foo.foo(1, false);
    }
}

public const MY_ERROR_REASON = "MyError";
# Custom Error
public type MyError error<record {| string message?; error cause?;  string...;|}>;

public class MockListener {

    *lang:Listener;

    public function __attach(service s, string? name) returns error? {
        return error("");
    }

    public function __detach(service s) returns error? {
        return error("");
    }

    public function __start() returns error? {
        return error("");
    }

    public function __gracefulStop() returns error? {
        return error("");
    }

    public function __immediateStop() returns error? {
        return error("");
    }
}

listener MockListener ep = new MockListener();

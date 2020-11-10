import ballerina/http;
import rasika/module2;

type Annot record {
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
    if(annotationVal is Annot) {
        annotationVal.foo.foo(1, false);
    }
}

//==================================================
// Util Functions and Objects
//==================================================

# Function Foo
# + a - a float
# + b - b bool
# + return - an integer
function foo(float a, boolean b) returns int {
    return 1;
}


# Function Foo or Error
# + a - a float
# + b - b bool
# + return - an integer
function fooErr(float a, boolean b) returns (int | error) {
    return 1;
}

# Function Bar
#
# + n - n float
# + return - future integer
function bar(float n) returns future<int> {
    return start foo(2, false);
}

function getMethods(int a) returns string[] {
    return ["get"];
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

public type Child object {
    # Returns foo
    # + a - float
    # + b - boolean
    public function foo(float a, boolean b) {

    }
};

# Returns bar
# + a - float
# + b - boolean
# + return - child
public function bar2(float a, boolean b) returns Child {
    return new Child();
}

public client class Stub {

    public Child obj = new Child();

    # Create a new Stub
    public function init(any arg) {

    }

    # Send a message
    # + msg - message
    public remote function send(string msg) {

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
    public function optionalBar(float a, boolean b) returns Child? {
        return ();
    }
}

type Employee record {
    int id;
    string name;
    int salary;
};

public const MY_ERROR_REASON = "MyError";
# Custom Error
type MyError error<record {| string message?; error cause?;  string...;|}>;

listener http:MockListener mockEP = new(9090);

public function actions() {
    //==================================================
    // Actions
    //==================================================
    // action :=
    //    start-action
    //    | wait-action
    //    | send-action
    //    | receive-action
    //    | flush-action
    //    | remote-method-call-action
    //    | checking-action
    //    | trap-action
    //    | ( action )
    // action-or-expr := action | expression
    // checking-action := checking-keyword action
    // trap-action := trap action

    // -- action, start-action
    future<int> r1 = start foo(2, true);

    // -- action, wait-action
    int a = wait bar(3);

    // -- action, send-action
    // -- action, receive-action
    worker w1 {
        foo(1, false) ->> w2;
    }
    worker w2 {
        int lw = <- w1;
    }

    // -- action, flush-action
    // flush w2;

    // -- action, remote-method-call-action
    Stub stub = new(1);
    stub->send("");

    // -- action, checking-action
    // int|error aa = check fooErr(2, false);
    int bb = checkpanic fooErr(1, false);

    // -- action, trap-action
    error? e = trap fooE(1, false);
}

public function statements() {
    //==================================================
    // Statements
    //==================================================
    // statement :=
    //    action-stmt
    //    | block-stmt
    //    | local-var-decl-stmt
    //    | local-type-defn-stmt
    //    | xmlns-decl-stmt
    //    | assignment-stmt
    //    | compound-assignment-stmt
    //    | destructuring-assignment-stmt
    //    | call-stmt
    //    | if-else-stmt
    //    | match-stmt
    //    | foreach-stmt
    //    | while-stmt
    //    | break-stmt
    //    | continue-stmt
    //    | fork-stmt
    //    | panic-stmt
    //    | return-stmt

    // -- statement, action-stmt
    // actions();

    // -- statement, block-stmt
    // { <statements> }

    // -- statement, local-var-decl-stmt
    // local-var-decl-stmt := local-init-var-decl-stmt | local-no-init-var-decl-stmt
    // local-init-var-decl-stmt :=[annots] [final] typed-binding-pattern = action-or-expr ;
    // local-no-init-var-decl-stmt := [annots] [final] type-descriptor variable-name ;

}

public function expressions() returns error? {
    //==================================================
    // Expressions
    //==================================================
    //     expression :=
    //    literal
    //    | list-constructor-expr
    //    | mapping-constructor-expr
    //    | table-constructor-expr
    //    | service-constructor-expr
    //    | string-template-expr
    //    | xml-expr
    //    | new-expr
    //    | variable-reference-expr
    //    | field-access-expr
    //    | optional-field-access-expr
    //    | annot-access-expr
    //    | member-access-expr
    //    | xml-attributes-expr
    //    | function-call-expr
    //    | method-call-expr
    //    | error-constructor-expr
    //    | anonymous-function-expr
    //    | arrow-function-expr
    //    | type-cast-expr
    //    | typeof-expr
    //    | unary-expr
    //    | multiplicative-expr
    //    | additive-expr
    //    | shift-expr
    //    | range-expr
    //    | numerical-comparison-expr
    //    | is-expr
    //    | equality-expr
    //    | binary-bitwise-expr
    //    | logical-expr
    //    | conditional-expr
    //    | checking-expr
    //    | trap-expr
    //    | ( expression )

    // -- expression, literal
    // "", 123,

    // -- expression, list-constructor-expr
    int[] list = [foo(1, false)];

    // -- expression, mapping-constructor-expr
    map<int> map1 = {"3": foo(1, true)};
    map<int> map2 = {[fooS(1, false)]: 1};

    //// -- expression, table-constructor-expr
    //table<Employee> tbEmployee = table {
    //    {key id, name, salary},
    //    [
    //        {1, "Mary",  300},
    //        {2, "John",  200},
    //        {3, "Jim", foo(1, false)}
    //    ]
    //};

    // -- expression, string-template-expr
    string s =string `${ foo(1, false) } !!!`;

    // -- expression, xml-expr
    xml x = xml `<foo>${ foo(1, false) }</foo>`;

    // -- expression, new-expr
    Stub stub1 = new(2);
    Stub stub2 = new Stub(2);

    // -- expression, variable-reference-expr
    int func = foo(1, false);

    // -- expression, field-access-expr
    Stub stub3 = new Stub(2);
    stub3.foo(1, false);
    stub3.bar(1, false).foo(1, false);
    stub3.obj.foo(1, false);

    // -- expression, optional-field-access-expr
    Stub stub4 = new Stub(2);
    stub4.obj.foo(1, false);
    // stub4.optionalBar(1, false)?.foo(1, false);

    // -- expression, annot-access-expr
    // annot-access-expr := expression .@ annot-tag-reference
    Annot? annot = fooT(1, false).@v1;

    // -- expression,  member-access-expr
    string[] arr = [];
    string s1 = fooA(1, false)[0];
    string s2 = arr[foo(1, false)];

    // -- expression, xml-attributes-expr
    //map<string>? attrs = fooX(1, false)@;

    // -- expression, function-call-expr
    int f1 = foo(1, false);
    int f2 = foo(fooF(1, false), false);
    int f3 = foo(a = fooF(1, false), b = false);
    int f4 = fooR(1, ...[foo(1, false)]);

    // -- expression, method-call-expr
    Stub stub5 = new Stub(2);
    stub5.obj.foo(1, false);

    // -- expression, error-constructor-expr
    MyError err1 = error();
    MyError err2 = MyError();

    // -- expression, anonymous-function-expr
    function (float, boolean) returns int func1 =
            function (float a, boolean b) returns int {
                return foo(1, false);
            };

    // -- expression, arrow-function-expr
    function (int, string) returns (string|int) func2 = (e, f) => foo(1, false);

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

    // -- expression, ( expression )
}

// -- service-constructor-expr
service hello on mockEP {
    @http:ResourceConfig {
        methods:getMethods(4),
        path:"/protocol"
    }
    resource function protocol(http:Caller caller, http:Request req) {
        http:Response res = new;
        json connectionJson = {protocol:caller.protocol};
        res.statusCode = 200;
        res.setJsonPayload(<@untainted json> connectionJson);
        checkpanic caller->respond(res);
    }
}

public function main() {
    actions();
    statements();
    error? e = expressions();
}

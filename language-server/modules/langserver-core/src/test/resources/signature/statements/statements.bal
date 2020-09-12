public function statements() returns error? {
    //==================================================
    // Statements
    //==================================================
    // statement :=
    // action-stmt
    // | block-stmt
    // | local-var-decl-stmt
    // | local-type-defn-stmt[!]
    // | xmlns-decl-stmt[!]
    // | assignment-stmt
    // | compound-assignment-stmt
    // | destructuring-assignment-stmt
    // | call-stmt
    // | if-else-stmt
    // | match-stmt
    // | foreach-stmt
    // | while-stmt
    // | break-stmt
    // | continue-stmt
    // | fork-stmt
    // | panic-stmt
    // | return-stmt
    // | lock-stmt
    //
    // NOTE: [!] Not related to signatureHelp

    // -- statement, action-stmt
    // actions();

    // -- statement, block-stmt
    // { <statements> }

    // -- statement, local-var-decl-stmt
    // local-var-decl-stmt := local-init-var-decl-stmt | local-no-init-var-decl-stmt
    // local-init-var-decl-stmt := [annots] [final] typed-binding-pattern = action-or-expr ;
    // local-no-init-var-decl-stmt := [annots] [final] type-descriptor variable-name ;
    int a = foo(1, false);

    // -- statement, local-type-defn-stmt
    // local-type-defn-stmt := [annots] type identifier type-descriptor ;

    // -- statement, xmlns-decl-stmt

    // -- statement, assignment-stmt
    int b;
    b = foo(1, false);

    // -- statement, compound-assignment-stmt
    int c = 0;
    c += foo(1, false);
    c -= foo(1, false);
    c *= foo(1, false);
    c /= foo(1, false);
    c &= foo(1, false);
    c |= foo(1, false);
    c ^= foo(1, false);
    c <<= foo(1, false);
    c >>= foo(1, false);
    c >>>= foo(1, false);

    // -- statement, destructuring-assignment-stmt
    // capture-binding-pattern
    var d = [1.1, false];
    d = fooTpl(1, false);
    // wildcard-binding-pattern
    _ = fooTpl(1, false);
    // list-binding-pattern
    float e;
    boolean f;
    [e, f] = fooTpl(1, false);
    // mapping-binding-pattern
    string g;
    string h;
    {firstName: g, lastName: h, age: _} = fooPerson(1, false);
    // functional-binding-pattern
    // Parser not supported yet

    //  -- statement, call-stmt
    // function-call-expr
    fooV(1, false);
    // method-call-expr
    Child i = new Child();
    i.foo(1, false);
    // checking-keyword call-expr
    check fooE(1, false);
    checkpanic fooE(1, false);

    //  -- statement, if-else-stmt
    if fooB(1, false) {
        fooV(1, false);
    } else if fooB(1, false) {
        fooV(1, false);
    } else {
        fooV(1, false);
    }

    //  -- statement, match-stmt
    match fooS(1, false) {
       "Mouse" => {
           fooV(1, false);
       }
       "Dog"|"Canine" => {
       }
       _ => {
       }
    }

    //  -- statement, foreach-stmt
    foreach var v in fooA(1, false) {
        fooV(1, false);
    }

    //  -- statement, while-stmt
    while fooB(1, false){
        fooV(1, false);
    }

    //  -- statement, break-stmt
    //break;

    //  -- statement, continue-stmt
    //continue;

    //  -- statement, fork-stmt
    //fork { named-worker-decl+ }

    //  -- statement, panic-stmt
    panic fooE(1, false);

    //  -- statement, return-stmt
    //return;

    //  -- statement, lock-stmt
    lock {
        fooV(1, false);
    }
}

# Function Foo
# + a - a float
# + b - b bool
# + return - an integer
function fooTpl(float a, boolean b) returns [float, boolean] {
    return [1.1, false];
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
function fooV(float a, boolean b) {

}

# Function Foo or Error
# + a - a float
# + b - b bool
# + return - an integer
function fooErr(float a, boolean b) returns (int | error) {
    return 1;
}

# Function Foo
# + a - a float
# + b - b bool
# + return - a string
function fooS(float a, boolean b) returns string {
    return "";
}

# Function Foo
# + a - a float
# + b - b bool
# + return - optional error
function fooE(int a, boolean b) returns error? {
    return ();
}

# Function Foo
# + a - a float
# + b - b bool
# + return - boolean
function fooB(float a, boolean b) returns boolean {
    return false;
}

# Function Foo
# + a - a float
# + b - b bool
# + return - int
function fooA(float a, boolean b) returns int[] {
    return [];
}

# Function Foo
# + a - a float
# + b - b bool
# + return - an integer
function fooPerson(float a, boolean b) returns Person {
    return {firstName:"john", lastName:"williams", age:40};
}

type Person record {|
   string firstName;
   string lastName;
   int age;
|};

public class Child {
    # Returns foo
    # + a - float
    # + b - boolean
    public function foo(float a, boolean b) {

    }
};

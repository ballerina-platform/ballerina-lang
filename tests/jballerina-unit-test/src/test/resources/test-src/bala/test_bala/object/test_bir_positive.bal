import bir/objs;

function f1(objs:Foo foo) {
    assertEquality(foo is objs:Foo, true);
}

function f2(objs:Baz baz) {
    assertEquality(baz is objs:Baz, true);
}

function f3(objs:Xyz xyz) {
    assertEquality(xyz is objs:Xyz, true);
}

function f4(objs:Qux qux) {
    assertEquality(qux is objs:Qux, true);
}

function f5(any foo) {
    assertEquality(foo is objs:Foo, true);
}

function f6(any baz) {
    assertEquality(baz is objs:Baz, true);
}

function f7(any xyz) {
    assertEquality(xyz is objs:Xyz, true);
}

function f8(any qux) {
    assertEquality(qux is objs:Qux, true);
}

function f9(any foo) {
    assertEquality(foo is objs:Bar, false);
}

function testObjectTypeAssignability() {
    objs:Foo foo = isolated client object {
         public isolated function execute() returns anydata|error {
           return "foo";
        }
    };
    
    objs:Baz baz = isolated client object {
        public isolated function execute() returns anydata|error {
            return "baz";
        }
    };
    
    objs:Xyz xyz = isolated service object {
        public isolated function execute() returns anydata|error {
            return "xyz";
        }
    };
    
    objs:Qux qux = isolated service object {
        public isolated function execute() returns anydata|error {
            return "qux";
        }
    };
    
    f1(foo);
    f2(baz);
    f3(xyz);
    f4(qux);
    f5(foo);
    f6(baz);
    f7(xyz);
    f8(qux);
    f9(foo);
}

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

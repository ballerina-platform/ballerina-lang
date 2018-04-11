type Person object {
    public {
        int age = 3,
        string name = "sample name";
    }
    private {
        int year = 5;
        string month = "february";
    }

    function attachedFn1(int a, float b) returns (int) {
        return 7 + a + <int>b;
    }

    function attachedFn2() returns (function (int, float) returns (int)) {
        var foo = (int a, float b) => (int) {
            return 7 + a + <int>b;
        };
        return foo;
    }

    function attachedFn3(int a, float b) returns (int);

    function attachedFn4() returns (function (int, float) returns (int));

    function attachedFn5(int a, float b) returns (function (float) returns ((function (boolean) returns (int)))) {
        var fooOut = (float f) => (function (boolean) returns (int)) {
            var fooIn = (boolean boo) => (int) {
                return 7 + a + <int>b + <int>f;
            };
            return fooIn;
        };
        return fooOut;
    }
};


function Person::attachedFn3(int a, float b) returns (int) {
    return a + <int>b;
}

function Person::attachedFn4() returns (function (int, float) returns (int)) {
    var foo = (int a, float b) => (int) {
        return 7 + a + <int>b;
    };
    return foo;
}

function test1() returns (int) {
    Person p = new;
    var foo = p.attachedFn1;
    return foo(43, 10.8);
}

function test2() returns (int) {
    Person p = new;
    var foo = p.attachedFn2;
    var bar = foo();
    return bar(43, 10.8);
}

function test3() returns (int) {
    Person p = new;
    var foo = p.attachedFn3;
    return foo(43, 10.8);
}

function test4() returns (int) {
    Person p = new;
    var foo = p.attachedFn4;
    var bar = foo();
    return bar(43, 10.8);
}

function test5() returns (int) {
    Person p = new;
    var foo = p.attachedFn5;
    var bar = foo(43, 10.8);
    var baz = bar(5.8);
    return baz(true);
}

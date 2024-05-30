function test1() {
    fn();
}

function test2(boolean condition) {
    if condition {
        fn();
    }
}

function test3(int val) {
    if val > 10 {
        if val > 100 {
            do {
                fn()
            }
        }
    }
}

function test4() {
    var anonFn = function() {
        fn();
    };
}

function test5() {
    MyClass myCl = new ();
    myCl->fn1();
    myCl.fn2();
    myCl->/path.accessor();
}

function test6() {
    error:cause(error("error"));
}

client class MyClass {
    remote function fn1() returns error? {

    }

    isolated function fn2() returns error? {

    }

    resource function accessor path() returns error? {

    }
}

function fn() returns error? {
}

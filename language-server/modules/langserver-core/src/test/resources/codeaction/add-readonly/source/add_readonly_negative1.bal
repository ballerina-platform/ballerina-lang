class MyClass {
    function fn() {

    }
}

isolated MyClass cls = new ();
isolated any val = new MyClass();

function fn1() returns MyClass {
    lock {
        return cls;
    }
}

function fn2() returns any {
    lock {
        return val;
    }
}

isolated function bar() {
    testFunction();
    return;
}

isolated int i = 1;

function fn1() {
    lock {
        i = testFunctionWithReturn();
    }
}

isolated function fn2() {
    lock {
        i = testFunctionWithReturn();
    }
}

isolated function fn3() {
    lock {
        testFunction();
    }
}

function fn4() {
    lock {
        i = testFunctionWithInput(i);
    }
}

isolated function fn5() {
    NonIsolatedClass cls1 = new;

    lock {
        IsolatedClass cls2 = new(i);
    }

    var cls3 = new IsolatedClass(i);

    cls1.fn();
    
    lock {
        cls3.fn(i);
    }
}

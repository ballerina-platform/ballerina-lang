connector TestConnector() {

    boolean action2Invoked;

    action actionWithAllTypesParams(int a, float b, string c = "John", int d = 5, string e = "Doe", int... z) 
                (int, float, string, int, string, int[]) {
        return a, b, c, d, e, z;
    }


    action actionWithoutRestParams(int a, float b, string c = "John", int d = 5, string e = "Doe") 
                (int, float, string, int, string) {
        return a, b, c, d, e;
    }
    
    action actionWithOnlyNamedParams(int a=5, float b=6.0, string c = "John", int d = 7, string e = "Doe") 
                (int, float, string, int, string) {
        return a, b, c, d, e;
    }

    action actionWithOnlyRestParam(int... z) (int[]) {
        return z;
    }
    
    
    action actionWithAnyRestParam(any... z) (any[]) {
        return z;
    }
}


function testInvokeActionInOrder1() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob", 40, 50, 60);
}

function testInvokeActionInOrder2() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    int[] array = [40, 50, 60];
    return testEndpoint.actionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob", ...array);
}

function testInvokeActionInMixOrder1() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithAllTypesParams(10, e="Bob", 20.0, c="Alex", 40, 50, d=30, 60);
}

function testInvokeActionInMixOrder2() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    int[] array = [40, 50, 60];
    return testEndpoint.actionWithAllTypesParams(10, e="Bob", 20.0, c="Alex", ...array, d=30);
}

function testInvokeActionWithoutRestArgs() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithAllTypesParams(10, e="Bob", 20.0, c="Alex", d=30);
}

function testInvokeActionWithoutSomeNamedArgs() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithAllTypesParams(c="Alex", 10, 20.0);
}

function testInvokeActionWithRequiredArgsOnly() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithAllTypesParams(10, 20.0);
}

function testInvokeActionWithRequiredAndRestArgs() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithAllTypesParams(10, 20.0, 40, 50, 60);
}

function funcInvocAsRestArgs() (int, float, string, int, string, int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob", ...getIntArray());
}

function getIntArray() (int[]) {
    return [1,2,3,4];
}

//------------- Testing an action having required and rest parameters --------

function testInvokeActionWithoutRestParams() (int, float, string, int, string) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithoutRestParams(10, e="Bob", 20.0, d=30);
}

//------------- Testing an action having only named parameters --------


function testInvokeActionWithOnlyNamedParams1() (int, float, string, int, string) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithOnlyNamedParams(b = 20, e="Bob", d=30, a=10 , c="Alex");
}

function testInvokeActionWithOnlyNamedParams2() (int, float, string, int, string) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithOnlyNamedParams(e="Bob", d=30, c="Alex");
}

function testInvokeActionWithOnlyNamedParams3() (int, float, string, int, string) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithOnlyNamedParams();
}

//------------- Testing an action having only rest parameters --------

function testInvokeActionWithOnlyRestParam1() (int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithOnlyRestParam();
}

function testInvokeActionWithOnlyRestParam2() (int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    return testEndpoint.actionWithOnlyRestParam(10, 20, 30);
}

function testInvokeActionWithOnlyRestParam3() (int[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    int[] a = [10, 20, 30];
    return testEndpoint.actionWithOnlyRestParam(...a);
}


//------------- Testing an action with rest parameter of any type --------

function testInvokeActionWithAnyRestParam1() (any[]) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector();
    }
    int[] a = [10, 20, 30];
    json j = {"name":"John"};
    return testEndpoint.actionWithAnyRestParam(a, j);
}

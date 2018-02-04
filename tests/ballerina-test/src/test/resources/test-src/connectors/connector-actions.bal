connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1() (boolean){
        return action2Invoked;
    }

    action action2() {
        action2Invoked = true;
    }
    
    action action3() (boolean) {
        return action2Invoked;
    }

    action action4() (string) {
        return param1;
    }

    action action5(string actionParam) (string, string, int) {
        return actionParam, param2, param3;
    }
}

connector EmptyParamConnector() {
    action emptyParamConnAction (string s) (string) {
        return s;
    }
}


function testAction1() (boolean) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector("MyParam1", "MyParam2", 5);
    }
    boolean value;

    value = testEndpoint.action1();
    return value;
}

function testAction2() {
    endpoint<TestConnector> testEndpoint {
        create TestConnector("MyParam1", "MyParam2", 5);
    }
    testEndpoint.action2();
}

function testAction3() (boolean) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector("MyParam1", "MyParam2", 5);
    }
    boolean value;

    value = testEndpoint.action3();
    return value;
}

function testAction2andAction3() (boolean) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector("MyParam1", "MyParam2", 5);
    }
    boolean value;

    testEndpoint.action2();

    value = testEndpoint.action3();
    return value;
}

function testAction4(string inputParam) (string) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector(inputParam, "MyParam2", 5);
    }
    string value;

    value = testEndpoint.action4();
    return value;
}

function testAction5(string functionArg1, string functionArg2, int functionArg3, string functionArg4) (string s1, string s2, int i) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector(functionArg1, functionArg2, functionArg3);
    }

    s1, s2, i = testEndpoint.action5(functionArg4);
    return;
}

function testEmptyParamAction(string inputParam) (string) {
    endpoint<EmptyParamConnector> testEndpoint {
        create EmptyParamConnector();
    }
    string s = testEndpoint.emptyParamConnAction(inputParam);
    return s;
}

function testDotActionInvocation(string functionArg1, string functionArg2, int functionArg3, string functionArg4) (string s1, string s2, int i) {
    endpoint<TestConnector> testEndpoint {
        create TestConnector(functionArg1, functionArg2, functionArg3);
    }

    s1, s2, i = testEndpoint.action5(functionArg4);
    return;
}

connector Foo (string name, int age) {
    int userAge = 10 + age;
    string userName = "Ballerina" + name;

    action getUserName () (string) {
        return userName;
    }

    action getAge () (int) {
        return userAge;
    }
}

connector Bar (string name) {
    Foo foo1 = create Foo("saman", 50);
    string userName = name;

    action returnConnectorFromAction () (Foo) {
        Foo foo = create Foo(userName, 1);
        return foo;
    }
    action returnFoo()(Foo) {
        return foo1;
    }

    action getAgeFromFoo(Foo foo) (int) {
        endpoint<Foo> ep {
            foo;
        }
        return ep.getAge();
    }
}

function testChainedActionInvocation()(int) {
    endpoint<Bar> ep {
        create Bar("aaaa");
    }
    return getAgeFromFoo(ep.returnFoo());
}

function getAgeFromFoo(Foo foo)(int) {
    endpoint<Foo> ep {
        foo;
    }
    return ep.getAge();
}

function getBar()(Bar) {
    Bar bar = create Bar("bbbbbb");
    return bar;
}

function testChainedFunctionActionInvocation()(int) {
    endpoint<Bar> ep {
        getBar();
    }
    return getAgeFromFoo(ep.returnFoo());
}

function getAge(Foo foo) (int) {
    endpoint<Foo> ep {
        foo;
    }
    return ep.getAge();
}

function testPassConnectorAsFunctionParameter() (int) {
    Foo foo = create Foo("abc", 77);
    return getAge(foo);
}

function testPassConnectorAsActionParameter() (int) {
    endpoint<Bar> bar {
        create Bar("ddd");
    }
    Foo foo = create Foo("abc", 20);
    return bar.getAgeFromFoo(foo);
}
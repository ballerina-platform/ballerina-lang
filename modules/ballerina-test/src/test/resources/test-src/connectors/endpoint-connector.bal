connector Foo() {
    action get(string path)(string) {
        return "Foo-" + path;
    }
}

connector Bar (Foo c, string para1) {
    action get1(string path)(string) {
        endpoint<Foo> en {
            c;
        }
        return "Bar-" + path + para1 + en.get(path);
    }
}

function testEndpointWithConnector() (string) {
    endpoint<Foo> ep {
        create Foo();
    }
    string retVal = ep.get("val1");
    return retVal;
}

function testCreateAsConnectorParam() (string) {
    endpoint<Bar> ep {
        create Bar(create Foo(), "-val2-");
    }
    return ep.get1("val1");
}

function testConnectorAsVarRef() (string) {
    Bar bar = create Bar(create Foo(), "-val2-");
    return invokeBarGet1(bar);
}

function invokeBarGet1(Bar bar) (string) {
    endpoint<Bar> ep {
        bar;
    }
    return ep.get1("val1");
}

connector FooFilter1 (Foo c, string para1) {
    action get(string path)(string) {
        endpoint<Foo> en {
            c;
        }
        return "FooFilter1-" + path + para1 + en.get(path);
    }
}

function testBaseConnectorEndpointType()(string) {
    endpoint<Foo> ep {
        create FooFilter1(create Foo(), "-val2-");
    }
    return ep.get("val1");
}

connector FooFilter2 (Foo c, string para1) {
    action get(string path)(string) {
        endpoint<Foo> en {
            c;
        }
        return "FooFilter2-" + path + para1 + en.get(path);
    }
}

function testCastingConnectorToBaseType()(string) {
    FooFilter1 ff = create FooFilter1((Foo)create FooFilter2(create Foo(), "para1-"), "para2-");
    return passFilterAsParamAndCast(ff);
}

function passFilterAsParamAndCast(FooFilter1 fo) (string) {
    endpoint<Foo> end {
        create FooFilter2((Foo)fo, "para3-");
    }
    return end.get("val1-");
}

function testBindConnectionToEndpoint()(string) {
    endpoint<Foo> ep {

    }
    FooFilter1 ff = create FooFilter1((Foo)create FooFilter2(create Foo(), "para1-"), "para2-");
    bind ff with ep;
    return ep.get("val1-");
}

function testEmptyEndpointInvocation()(string) {
    endpoint<Foo> ep {

    }
    return ep.get("val1");
}

function testBindWhichGetsConFromFunction()(string) {
    endpoint<Foo> ep {

    }
    bind getFoo() with ep;
    return ep.get("val1");
}

function getFoo()(Foo) {
    Foo fo = create Foo();
    return fo;
}

function testEndpointInWorker()(string) {
    endpoint<Foo> foo {

    }
    worker w1 {
        bind create FooFilter1(create Foo(), "para1-") with foo;
        "val1-" -> w2;
        string result;
        result <- w2;
        result = result + foo.get("val2-");
        return result;
    }

    worker w2 {
        string m;
        m <- w1;
        bind create FooFilter2(create Foo(), "para2-") with foo;
        m = foo.get(m);
        m -> w1;
    }
}

struct Person {
    string name;
    int age;
}

connector Bar1 (string para1) {
    Person person = {name:"tyler", age:45};
    action get1(string prefix)(string) {
        return prefix + person.name;
    }
}

function testConnectorWithStructVar()(string) {
    endpoint<Bar1> ep {
        create Bar1("getName");
    }
    return ep.get1("name - ");
}

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

connector FooFilter1 (Foo c, string para1) {
    action get(string path)(string) {
        endpoint<Foo> en {
            c;
        }
        return "FooFilter1-" + path + para1 + en.get(path);
    }
}

function invalidActionInvocation() {
    Foo fo = create Foo();
    string ll = fo.get("ss");
}

function incompatibleConnectorAssignment() {
    endpoint<Bar> en {
        create Foo();
    }
}

function undefinedConnectorInEndpoint() {
    endpoint<TestConnector> endp {

    }
}

function invalidEndpointAssignment() {
    endpoint<Foo> en {

    }
    Foo foo = create Foo();
    en = foo;
    string ll = "dd";
    en = ll;
    en = create Foo();
}
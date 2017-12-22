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

function unreachableBind()(string) {
    endpoint<Foo> en {

    }
    Foo foo = create Foo();
    return "dddd";
    bind foo with en;
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

function passEndpointAsParam() {
    endpoint<Foo> fo {
        create Foo();
    }
    endpointAsParam(fo);
}

function endpointAsParam(Foo fo) {

}

function bindWrongType() {
    endpoint<Foo> ep {

    }
    bind create Bar(create Foo(), "dddd") with ep;
}

function bindStringValue() {
    endpoint<Foo> ep {

    }
    bind "test" with ep;
}

function returnEndpoint()(Foo) {
    endpoint<Foo> fo {

    }
    return fo;
}

function initEndointWithString(string val) {
    endpoint<Foo> fo {
        val;
    }
}

connector ConWithWrongActions() {
    action get()(string) {
        string value = "sample value";
    }

    action get1()(string value) {
        value = "Foo-";
    }
 }
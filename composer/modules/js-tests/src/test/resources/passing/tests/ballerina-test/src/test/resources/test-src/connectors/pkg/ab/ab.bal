package pkg.ab;

import pkg.ef;
import pkg.cd;

connector BarFilter2 (ef:Bar c, string para1) {
    action get1(string path)(string) {
        endpoint<ef:Bar> en {
            c;
        }
        return "BarFilter2-" + path + para1 + en.get1(path);
    }
}

connector FooFilter2 (ef:Foo c, string para1) {
    action get(string path)(string) {
        endpoint<ef:Foo> en {
            c;
        }
        return "FooFilter2-" + path + para1 + en.get(path);
    }
}

function testEndpointWithConnector() (string) {
    endpoint<ef:Foo> ep {
        create ef:Foo();
    }
    string retVal = ep.get("val1");
    return retVal;
}

function testCreateAsConnectorParam() (string) {
    endpoint<ef:Bar> ep {
        create ef:Bar(create ef:Foo(), "-val2-");
    }
    return ep.get1("val1");
}

function testConnectorAsVarRef() (string) {
    ef:Bar bar = create ef:Bar(create ef:Foo(), "-val2-");
    return invokeBarGet1(bar);
}

function invokeBarGet1(ef:Bar bar) (string) {
    endpoint<ef:Bar> ep {
        bar;
    }
    return ep.get1("val1");
}

function testBaseConnectorEndpointType()(string) {
    endpoint<ef:Foo> ep {
        create cd:FooFilter1(create ef:Foo(), "-val2-");
    }
    return ep.get("val1");
}



function testCastingConnectorToBaseType()(string) {
    cd:FooFilter1 ff = create cd:FooFilter1((ef:Foo)create FooFilter2(create ef:Foo(), "para1-"), "para2-");
    return passFilterAsParamAndCast(ff);
}

function passFilterAsParamAndCast(cd:FooFilter1 fo) (string) {
    endpoint<ef:Foo> end {
        create FooFilter2((ef:Foo)fo, "para3-");
    }
    return end.get("val1-");
}

function testBindConnectionToEndpoint()(string) {
    endpoint<ef:Foo> ep {

    }
    cd:FooFilter1 ff = create cd:FooFilter1((ef:Foo)create FooFilter2(create ef:Foo(), "para1-"), "para2-");
    bind ff with ep;
    return ep.get("val1-");
}



import testorg/foo;

function testCheck () {
    var a = testCheckFunction();

    if (a is error) {
        if (a.message() == "i1") {
            return;
        }
        panic error("Expected error message: i1, found: " + a.message());
    }
    panic error("Expected error, found: " + (typeof a).toString());
}

function testCheckFunction () returns error? {
    foo:DummyEndpoint dyEP = foo:getDummyEndpoint();
    check dyEP->invoke1("foo");
    return ();
}

function testNewEP(string a) {
    foo:DummyEndpoint ep1 = new;
    string r = ep1->invoke2(a);

    if (r == "donedone") {
        return;
    }
    panic error("Expected donedone, found: " + r);
}

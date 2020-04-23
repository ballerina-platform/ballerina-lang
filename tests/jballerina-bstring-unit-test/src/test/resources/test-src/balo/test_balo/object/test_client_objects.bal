import testorg/foo;

function testCheck () returns error? {
    var a = testCheckFunction();
    return a;
}

function testCheckFunction () returns error? {
    foo:DummyEndpoint dyEP = foo:getDummyEndpoint();
    check dyEP->invoke1("foo");
    return ();
}

function testNewEP(string a) returns string {
    foo:DummyEndpoint ep1 = new;
    string r = ep1->invoke2(a);
    return r;
}
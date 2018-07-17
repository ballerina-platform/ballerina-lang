
import pkg.ab;

function testCheck () returns error? {
    var a = testCheckFunction();
    return a;
}

function testCheckFunction () returns error?{
    check ab:ep -> invoke1("foo");
    return ();
}

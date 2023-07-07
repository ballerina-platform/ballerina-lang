import bir/objs;

function f1(objs:Bar bar) {
    assertEquality(bar is objs:Foo, false);
}

function f2(objs:Foo foo) {
    assertEquality(foo is objs:Bar, false);
}

function f3(objs:Xyz xyz) {
    assertEquality(xyz is objs:Qux, false);
}

function f4(objs:Qux qux) {
    assertEquality(qux is objs:Xyz, false);
}

function assertEquality(any|error actual, any|error expected) {
}

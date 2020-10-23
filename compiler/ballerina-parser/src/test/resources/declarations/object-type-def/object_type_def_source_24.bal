class Foo {
    isolated client object {} a = b;
    public isolated client object {} c = d;
}

isolated object {} a;

function foo(isolated object {} obj) returns isolated object {} {
    isolated object {} c;
    isolated object {}|MyType[] d;
    [isolated object {}, int, restType...] e;

    boolean b = x is isolated object {};
    f2 = <isolated object {}>f1;
}

type bar record {
    isolated object {} obj;
};

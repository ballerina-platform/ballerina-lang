
type Foo distinct error;

function testFooError() returns Foo {
    Foo foo = Foo("error message", detailField=true);
    var x = foo.detail();
    Foo f = foo;

    return foo;
}

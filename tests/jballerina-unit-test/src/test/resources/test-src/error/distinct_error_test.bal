
type Foo distinct error;

function testFooError() returns Foo {
    //Foo foo = Foo("error message");
    Foo foo = error("error message");
    Foo f = foo;

    return foo;
}

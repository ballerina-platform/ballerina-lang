
type Foo distinct error;
type Fee distinct Foo;
type Bar distinct error<record {| int code; |}>;

function testFooError() returns Foo {
    //Foo foo = Foo("error message");
    Bar b = Bar("message");

    error foo = Foo("error message"); // Can assign distinct type to default error.
    Foo f = foo; // Cannot assign error to distinct error type
    Fee e = Fee("message");
    Foo k = e;

    return foo;
}

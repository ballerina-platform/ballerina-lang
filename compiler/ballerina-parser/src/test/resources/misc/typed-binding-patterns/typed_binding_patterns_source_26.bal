Foo[] & Bar? [] = expr;

Foo[]|Bar? [a, b, c] = expr;

Foo[]|Bar?[] [] = expr;

Foo[]|Bar?[] d = expr;

Foo[]|Bar?[]|Baz [] = expr;

Foo[]|Bar?[]|Baz[] e = expr;

function fooBar() {
    Foo[] & Bar? [] = expr;

    Foo[]|Bar? [a, b, c] = expr;

    Foo[]|Bar?[] [] = expr;

    Foo[]|Bar?[] d = expr;

    Foo[]|Bar?[]|Baz [] = expr;

    Foo[]|Bar?[]|Baz[] e = expr;
}

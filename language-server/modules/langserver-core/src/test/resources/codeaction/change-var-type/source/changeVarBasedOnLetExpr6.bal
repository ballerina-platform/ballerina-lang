class Foo {
    int test = let int x = 2 in getString();
}

function getString() returns string {
    return "";
}

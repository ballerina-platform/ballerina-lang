function foo() {
    <- default;
    <- b;
    a = <- b;
    a = <- {a};
    a = <- {a, b:c, d};
    <- {default, b:default, d};
}

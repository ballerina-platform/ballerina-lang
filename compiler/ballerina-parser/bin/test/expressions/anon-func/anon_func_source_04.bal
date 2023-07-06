function foo() {
    x = function() => foo() + function() => foo() + y;
}

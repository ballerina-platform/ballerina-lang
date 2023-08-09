function foo() {
    _ = check doA() on fail e => error("Error occurred", e) is int ? 1 : 0;
        check doA() on fail e => doB() is int ? 1 : 0;
}

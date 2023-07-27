function foo() {
    val = check doA() on fail e => error("Error occurred", e) is int ? 1 : 0;
}
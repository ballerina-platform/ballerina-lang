function test() {
    check doA() fail e => error("oops!");
    checkpanic doA() fail e => error("oops!");
    _ = check doA() fail e => error("oops!");
    _ = checkpanic doA() fail e => error("oops!");
}

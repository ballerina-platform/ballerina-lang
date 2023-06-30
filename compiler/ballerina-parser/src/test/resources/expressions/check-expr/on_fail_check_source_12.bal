function test() {
    check doA() fail e error("oops!");
    _ = checkpanic doA() fail e error("oops!");
}

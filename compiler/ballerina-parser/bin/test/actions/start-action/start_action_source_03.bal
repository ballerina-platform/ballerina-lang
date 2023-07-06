function foo() {
    f1 = @strand {thread: "any"} start add_1();

    @strand {thread: "any"}
    start add_2();
}

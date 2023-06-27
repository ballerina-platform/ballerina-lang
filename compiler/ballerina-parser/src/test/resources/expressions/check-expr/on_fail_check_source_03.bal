function test() {
    checkpanic doA() on fail e error("Something happened", e, c1 = c1, c2 = c2);
}

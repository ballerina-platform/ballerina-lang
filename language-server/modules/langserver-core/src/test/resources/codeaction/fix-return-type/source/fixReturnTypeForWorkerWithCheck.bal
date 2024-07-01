function testFunction() {
    worker w2 returns string {
        error|string e = error("test");
        _ = check e;
    }
}

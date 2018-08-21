function test() {
    worker w1 {
        int/*def*/ a;
        /*ref*/a = 10;
    }
}

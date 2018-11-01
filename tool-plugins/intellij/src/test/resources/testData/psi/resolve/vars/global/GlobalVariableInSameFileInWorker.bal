int /*def*/a;

function test() {
    worker w1 {
        /*ref*/a = 10;
    }
}

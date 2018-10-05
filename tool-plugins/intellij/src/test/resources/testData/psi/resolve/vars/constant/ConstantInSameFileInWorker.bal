@final int /*def*/a;

function test() {
    worker w1 {
        int value = /*ref*/a;
    }
}

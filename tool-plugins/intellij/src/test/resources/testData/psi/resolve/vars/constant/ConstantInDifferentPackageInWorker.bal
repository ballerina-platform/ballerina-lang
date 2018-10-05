import org/test;

function test() {
    worker w1 {
        int value = test:/*ref*/a;
    }
}

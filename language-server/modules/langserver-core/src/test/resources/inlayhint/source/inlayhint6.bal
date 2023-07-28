class TestClass {
    function init(int x, int... y) {

    }
}

function testFunction() {
    int arr = [1, 2, 3];
    TestClass testClass = new(0, ...arr);
}

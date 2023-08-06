function returnInt() returns int => 3;

function testCheckedOnFail() {
    int val = check returnInt() o is int ? 1 : 0;
}

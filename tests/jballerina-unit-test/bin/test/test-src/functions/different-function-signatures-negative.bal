function funcInvocAsRestArgs() returns [int, float, string, int, string, int[]] {
    bar(5, "Alex", 6, 7);
}

function bar(int a, string b = "John", int... z) {
}

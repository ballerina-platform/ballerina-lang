function foo() returns [int, string...] {
    [int, string] a = [4, "hello"];
    int[1]|[int, int...] arr = [1, 2, 3];
    return a;
}

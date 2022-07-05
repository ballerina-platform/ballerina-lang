function foo() {
    future<int> f1 = @s
    int x = 3;
    future<int> f2 = @strand {

    }
    var f3 = @s1 @s2 {}
}

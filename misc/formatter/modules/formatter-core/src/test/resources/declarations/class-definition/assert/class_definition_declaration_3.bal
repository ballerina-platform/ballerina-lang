class Foo {
    public remote function foo() {
        int x = 0;
    }

    public remote function bar() returns int {
        int x = 0;
        return x * 2;
    }
}

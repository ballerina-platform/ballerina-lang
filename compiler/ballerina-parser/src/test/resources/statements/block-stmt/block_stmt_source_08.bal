function testBlockStmt() {
    {
        foo:foobar("a");
        foo:foobar("b");
        {
            foo:foobar("c");
            {
                foo:foobar("d");
            }
        }
        foo:foobar("e");
    }
    foo:foobar("f");
}

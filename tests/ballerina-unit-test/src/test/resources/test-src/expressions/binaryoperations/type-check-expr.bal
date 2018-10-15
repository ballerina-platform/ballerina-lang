function testTypeCheckExpr() returns string {
    int|string x = "hello";
    int y = 10;
    if (y is int) {
        return "int";
    } else {
        return "string";
    }
}

int[] intArr = [];
map<json> jsonMap = {};
ReadonlyClass readonlyClass = new ();
IsolatedClass isolatedClass = new (1);
NonIsolatedClass nonIsolatedClass = new ();

isolated function fn6() returns int {
    return intArr.length() + jsonMap.length();
}

isolated function fn7() {
    readonlyClass.fn(10);
    isolatedClass.fn2(11);
    nonIsolatedClass.fn2(12);
}

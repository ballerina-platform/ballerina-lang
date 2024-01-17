function testFunction() {
    future<int> testFutureInt = start returnInt();
    future<string> testFutureString = start returnString();
    string testString = check wait ;

}

function returnString() returns string {
    return "";
}

function returnInt() returns int {
    return 1;
}

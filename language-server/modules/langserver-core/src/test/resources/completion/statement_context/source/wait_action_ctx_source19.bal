function testFunction() {
    future<int> testFutureInt = start returnInt();
    future<string:Char> testFutureChar = start returnChar();
    future<string> testFutureString = start returnString();
    string testString = check wait ;
}

function returnInt() returns int {
    return 1;
}

function returnChar() returns string:Char {
    return <string:Char>"";
}

function returnString() returns string {
    return "";
}


public const int TEST_INT_CONST1 = 1;

public const string TEST_STRING_CONST1 = "HELLO WORLD";

const int TEST_INT_CONST2 = 2;

const string TEST_STRING_CONST2 = "HELLO WORLD2";

public future<int> TEST_FUTURE_INT = getFutureInt();

function getInt() returns int {
    return 1;
}

function getFutureInt() returns future<int> {
    return start getInt();
}
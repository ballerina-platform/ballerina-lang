const int a = 6;

function testConditionScope(int b)(int) {
    int output = 0;
    if (a > b) {
        int a = 1;
        output = 10;
    } else if (a == b) {
        int a = 2;
        output = 20;
    }
    return output;
}
function arrayLengthAccessTestAssignmentCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = (lengthof arr);
    return length;
}

function arrayLengthAccessTestFunctionInvocationCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arrayLength(lengthof arr);
    return length;
}


function arrayLength(int x) (int) {
    return x;
}

function arrayLengthAccessTestVariableDefinitionCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = arrayLength(lengthof arr);
    return length;
}

function arrayLengthAccessTestArrayInitializerCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] tempArr = [(lengthof arr),(x+y)];
    return tempArr[0];
}

function arrayLengthAccessTestMapInitializerCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    map tempMap = {"length":(lengthof arr)};
    int length;
    length, _ = (int) tempMap["length"];
    return length;
}

function arrayLengthAccessTestReturnStatementCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    return (lengthof arr);
}

function arrayLengthAccessTestMultiReturnStatementCase(int x, int y) (int,int,int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] brr = [];
    brr[0] = 1;
    int[] crr = [];
    crr[0] = 1;
    crr[1] = x + y;
    return (lengthof arr), (lengthof brr), (lengthof crr);
}

function arrayLengthAccessTestTypeCastExpressionCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = (int) (lengthof arr);
    return length;
}

function arrayLengthAccessTestIfConditionCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if( (lengthof arr) == 3 ) {
       return 3;
    } else{
       return 0;
    }
}

function arrayLengthAccessTestBinaryExpressionCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if((lengthof arr) == (lengthof arr)) {
       return 3;
    } else{
       return 0;
    }
}

function arrayLengthAccessTestStructFieldAccessCase(int x, int y) (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    Person jack;
    jack = {name:"Jack", days:arr};

    if ((lengthof jack.days) == 3 ) {
        return 3;
    } else {
        return 0;
    }
}

struct Person {
    string name;
    int[] days;
}

function arrayLengthAccessTestJSONArrayCase(int x, int y) (int) {
    json arr = [x,y,5,5,6,6];
    int length;
    length = (lengthof arr);
    return length;
}

function arrayLengthAccessTestJSONArrayNegativeNonArrayCase(int x, int y) (int) {
    json arr = {"number1":x, "number2":y};
    int length;
    length = (lengthof arr);
    return length;
}

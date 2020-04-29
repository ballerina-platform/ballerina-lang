function arrayLengthAccessTestAssignmentCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arr.length();
    return length;
}

function arrayLengthAccessTestFunctionInvocationCase(int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arrayLength(arr.length());
    return length;
}


function arrayLength(int x) returns (int) {
    return x;
}

function arrayLengthAccessTestVariableDefinitionCase(int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = arrayLength(arr.length());
    return length;
}

function arrayLengthAccessTestArrayInitializerCase(int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] tempArr = [arr.length(),(x+y)];
    return tempArr[0];
}

function arrayLengthAccessTestMapInitializerCase(int x, int y) returns (int|error) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    map<any> tempMap = {"length":arr.length()};
    int length;
    length = <int>tempMap["length"];
    return length;
}

function arrayLengthAccessTestReturnStatementCase(int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    return arr.length();
}

function arrayLengthAccessTestMultiReturnStatementCase(int x, int y) returns [int,int,int] {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] brr = [];
    brr[0] = 1;
    int[] crr = [];
    crr[0] = 1;
    crr[1] = x + y;
    return [arr.length(), brr.length(), crr.length()];
}

function arrayLengthAccessTestTypeCastExpressionCase(int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = arr.length();
    return length;
}

function arrayLengthAccessTestIfConditionCase(int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if( arr.length() == 3 ) {
       return 3;
    } else{
       return 0;
    }
}

function arrayLengthAccessTestBinaryExpressionCase(int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if( arr.length() == arr.length() ) {
       return 3;
    } else{
       return 0;
    }
}

function arrayLengthAccessTestStructFieldAccessCase(int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];

    Person jack = {name:"Jack", days:arr};

    if ( jack.days.length() == 3 ) {
        return 3;
    } else {
        return 0;
    }
}

function arrayLengthAccessNullArrayCase(int x, int y) returns (int) {
    int z = x + y;
    int[] arr = [];
    int length;
    length = arr.length();
    return length;
}

type Person record {
    string name;
    int[] days;
};

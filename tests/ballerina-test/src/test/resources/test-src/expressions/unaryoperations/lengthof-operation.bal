function arrayLengthAccessTestAssignmentCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = (lengthof arr);
    return length;
}

function arrayLengthAccessTestFunctionInvocationCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arrayLength(lengthof arr);
    return length;
}

function arrayLength (int x) returns (int) {
    return x;
}

function arrayLengthAccessTestVariableDefinitionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = arrayLength(lengthof arr);
    return length;
}

function arrayLengthAccessTestArrayInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] tempArr = [(lengthof arr),(x+y)];
    return tempArr[0];
}

function arrayLengthAccessTestMapInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    map tempMap = {"length":(lengthof arr)};
    int length;
    length =check <int> tempMap["length"];
    return length;
}

function arrayLengthAccessTestReturnStatementCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    return (lengthof arr);
}

function arrayLengthAccessTestMultiReturnStatementCase (int x, int y) returns (int,int,int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] brr = [];
    brr[0] = 1;
    int[] crr = [];
    crr[0] = 1;
    crr[1] = x + y;
    return ((lengthof arr), (lengthof brr), (lengthof crr));
}

function arrayLengthAccessTestTypeCastExpressionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = <int> (lengthof arr);
    return length;
}

function arrayLengthAccessTestIfConditionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if ((lengthof arr) == 3) {
       return 3;
    } else{
       return 0;
    }
}

function arrayLengthAccessTestBinaryExpressionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if ((lengthof arr) == (lengthof arr)) {
       return 3;
    } else {
       return 0;
    }
}

function arrayLengthAccessTestStructFieldAccessCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    Person jack = {name:"Jack", days:arr};

    if ((lengthof jack.days) == 3) {
        return 3;
    } else {
        return 0;
    }
}

type Person {
    string name;
    int[] days;
};

function arrayLengthAccessTestJSONArrayCase (int x, int y) returns (int) {
    json arr = [x,y,5,5,6,6];
    int length;
    length = (lengthof arr);
    return length;
}

function arrayLengthAccessTestJSONArrayNegativeNonArrayCase (int x, int y) returns (int) {
    json arr = {"number1":x, "number2":y};
    int length;
    length = (lengthof arr);
    return length;
}


function lengthOfMap (int x, int y) returns (int) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    int length = lengthof namesMap;
    return length;
}

function lengthOfMapEmpty (int x, int y) returns (int) {
    map namesMap;
    int length = lengthof namesMap;
    return length;
}

function lengthOfString() returns (int, int, int) {
    string foo = "hello world";
    int l1 = lengthof foo;
    int l2 = lengthof "John";
    int l3 = lengthof string `Hello {{"John"}}`;
    return (l1, l2, l3);
}

function lengthOfBlob() returns (int, int) {
    string s1 = "Hello";
	blob b1 = s1.toBlob("UTF-8");
    int l1 = lengthof b1;
    
    string s2 = "";
    blob b2 = s2.toBlob("UTF-8");
    int l2 = lengthof b2;
    
    return (l1, l2);
}

function lengthOfNullString() returns (int) {
    string foo;
    return lengthof foo;
}

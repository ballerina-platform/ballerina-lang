function arrayLengthAccessTestAssignmentCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = (arr.length());
    return length;
}

function arrayLengthAccessTestFunctionInvocationCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arrayLength(arr.length());
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
    int length = arrayLength(arr.length());
    return length;
}

function arrayLengthAccessTestArrayInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] tempArr = [(arr.length()),(x+y)];
    return tempArr[0];
}

function arrayLengthAccessTestMapInitializerCase (int x, int y) returns (int|error) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    map<any> tempMap = {"length":(arr.length())};
    int length;
    length = <int>tempMap["length"];
    return length;
}

function arrayLengthAccessTestReturnStatementCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    return (arr.length());
}

function arrayLengthAccessTestMultiReturnStatementCase (int x, int y) returns [int,int,int] {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] brr = [];
    brr[0] = 1;
    int[] crr = [];
    crr[0] = 1;
    crr[1] = x + y;
    return [(arr.length()), (brr.length()), (crr.length())];
}

function arrayLengthAccessTestTypeCastExpressionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = <int> (arr.length());
    return length;
}

function arrayLengthAccessTestIfConditionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if ((arr.length()) == 3) {
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
    if ((arr.length()) == (arr.length())) {
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

    if ((jack.days.length()) == 3) {
        return 3;
    } else {
        return 0;
    }
}

type Person record {
    string name;
    int[] days;
};

function arrayLengthAccessTestJSONArrayCase (int x, int y) returns (int) {
    json arr = [x,y,5,5,6,6];
    int length = 0;
    if (arr is json[]) {
        length = (arr.length());
    }
    return length;
}

function lengthOfMap (int x, int y) returns (int) {
    map<any> namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    int length = namesMap.length();
    return length;
}

function lengthOfMapEmpty (int x, int y) returns (int) {
    map<any> namesMap = {};
    int length = namesMap.length();
    return length;
}

function lengthOfString() returns [int, int, int] {
    string foo = "hello world";
    int l1 = foo.length();
    string s1 = "John";
    int l2 = s1.length();
    string s2 =string `Hello ${"John"}`;
    int l3 = s2.length();
    return [l1, l2, l3];
}

function lengthOfBlob() returns [int, int] {
    string s1 = "Hello";
	byte[] b1 = s1.toBytes();
    int l1 = b1.length();
    
    string s2 = "";
    byte[] b2 = s2.toBytes();
    int l2 = b2.length();
    
    return [l1, l2];
}

function lengthOfNullString() returns (int) {
    string foo = "";
    return foo.length();
}

function lengthOfJSONObject() returns (int) {
    map<json> j = {"a":"A", "b":"B"};
    return j.length();
}

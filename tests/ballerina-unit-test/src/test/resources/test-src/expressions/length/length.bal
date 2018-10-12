function arrayLengthAccessTestAssignmentCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int lengthVal;
    lengthVal = (arr.length());
    return lengthVal;
}

function arrayLengthAccessTestFunctionInvocationCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int lengthVal;
    lengthVal = arrayLength(arr.length());
    return lengthVal;
}

function arrayLength (int x) returns (int) {
    return x;
}

function arrayLengthAccessTestVariableDefinitionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int lengthVal = arrayLength(arr.length());
    return lengthVal;
}

function arrayLengthAccessTestArrayInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] tempArr = [(arr.length()),(x+y)];
    return tempArr[0];
}

function arrayLengthAccessTestMapInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    map tempMap = {"length":(arr.length())};
    int lengthVal;
    lengthVal =check <int> tempMap.length;
    return lengthVal;
}

function arrayLengthAccessTestReturnStatementCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    return (arr.length());
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
    return ((arr.length()), (brr.length()), (crr.length()));
}

function arrayLengthAccessTestTypeCastExpressionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int lengthVal = <int> (arr.length());
    return lengthVal;
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

type Employee record {
    int id;
    string name;
    float salary;
};

type Empty record {};

function arrayLengthAccessTestJSONArrayCase (int x, int y) returns (int) {
    json arr = [x,y,5,5,6,6];
    int lengthVal;
    lengthVal = (arr.length());
    return lengthVal;
}

function lengthOfMap () returns (int) {
    map namesMap = {fname:"foo", lname:"bar", sname:"abc", tname:"pqr"};
    int lengthVal = namesMap.length();
    return lengthVal;
}

function lengthOfMapEmpty () returns (int) {
    map namesMap;
    int lengthVal = namesMap.length();
    return lengthVal;
}

function lengthOfSingleXmlElement() returns (int) {
    xml x1 = xml `<book>The Lost World</book>`;
    int lengthVal = x1.length();
    return lengthVal;
}

function lengthOfMultipleXmlElements() returns (int) {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `Hello, world!`;
    xml x3 = xml `<!--I am a comment-->`;
    xml x4 = xml `<?target data?>`;
    xml x5 = x1 + x2 + x3 + x4;
    int lengthVal = x5.length();
    return lengthVal;
}

function lengthOfTuple() returns (int) {
    (int, string) a = (10, "John");
    int lengthVal = a.length();
    return lengthVal;
}

function lengthOfTable() returns (int) {
    table<Employee> tbEmployee = table {
        { primarykey id, name, salary },
        [ { 1, "Mary",  300.5 },
          { 2, "John",  200.5 },
          { 3, "Jim", 330.5 }
        ]
    };
    int lengthVal = tbEmployee.length();
    return lengthVal;
}

function lengthOfEmptyTable() returns (int) {
    table<Employee> tbEmployee = table {
        { primarykey id, name, salary }
    };
    int lengthVal = tbEmployee.length();
    return lengthVal;
}

function lengthOfRecord() returns (int) {
    Employee emp = {id : 1 , name : "John", salary: 300};
    int lengthVal = emp.length();
    return lengthVal;
}

function lengthOfEmptyRecord() returns (int) {
    Empty emp = {};
    int lengthVal = emp.length();
    return lengthVal;
}
function testIfStmt(int aa, int b, int c) returns [int, int] {

    int x;
    x = 10;

    int a1;
    if ( aa == b) {
        a1 = 100;

    } else if (aa == b + 1){
        a1 = 200;

    } else  if (aa == b + 2){
        a1 = 300;

    }  else {
        a1 = 400;
    }

    int b1 = c;

    return [a1 + x, b1 + 1];
}

function testIfStmtWithoutParentheses(int aa, int b, int c) returns [int, int] {

    int x;
    x = 10;

    int a1;
    if aa == b {
        a1 = 100;

    } else if aa == (b + 1) {
        a1 = 200;

    } else  if (((aa == (b + 2)))) {
        a1 = 300;

    } else {
        a1 = 400;
    }

    int b1 = c;

    return [a1 + x, b1 + 1];
}


function testAgeGroup(int age) returns (string) {
    string avgGroup;
    if (age > 18) {
        avgGroup = "elder";
    } else {
        avgGroup = "minor";
    }
    return avgGroup;
}

function ifElseScope(int number) returns (int) {
int i = number;
    if(i == 1) {
        i = -10;
        int j = 2;
        if(j == 2) {
            int k = 200;
            i = k;
        } else {
            int k = -1;
        }
      } else if (i == 2) {
         int j = 400;
         i = j;
       } else {
         i = 100;
         int j = 500;
         i = j;
    }
    return i;
}

function nestedIfElseScope(int number1, int number2) returns (int) {
    int i = number1;
    if(i == 1) {
        int j = number2;
        if(j == 1) {
            int k = 100;
            i = k;
        } else {
            int k = 200;
            i = k;
        }
    } else if (i == 2) {
        int j = number2;
        i = j;
        if(j == 2) {
            int k = 300;
            i = k;
        } else {
            int k = 400;
            i =k;
        }
    } else {
        i = 100;
        int j = number2;
        if(j == 3) {
            int k = 500;
            i = k;
        } else {
            int k = 600;
        i = k;
        }
    }
    return i;
}

final int a = 6;

function testConditionScope(int b) returns (int) {
    int output = 0;
    if (a > b) {
        int c = 1;
        output = 10;
    } else if (a == b) {
        int c = 2;
        output = 20;
    }
    return output;
}

const ONE = 1;

function testTypeNarrowing(string? s) returns string {
    int|boolean a = 5;
    if a == ONE {
        ONE b = a;
    } else {
        int|boolean c = a;
    }

    if a == 5 {
        5 b = a;
    } else {
        int|boolean c = a;
    }

    if () == s {
        () t = s;
    } else {
        string u = s;
    }

    // Narrow type for !=
    if s != () {
        string u = s;
    } else {
        () t = s;
    }

    if s == () {
        return "";
    } else {
        return s;
    }
}

function testTypeNarrowingWithLambda() {
    string? optionalName = "Ballerina";
    var lambdaFunc = function (int|string id) returns ONE {
        int? optionalAge = 20;
        if optionalAge == () {
            () d = optionalAge;
        }

        if id != ONE {
            return 1;
        } else {
            return id;
        }
    };
}

function testResetTypeNarrowingForCompoundAssignment() {
    int a = 5;
    if a == 5 {
        a += 1;
    }
}

function testResetTypeNarrowing() {
    int a = 10;
    if a > 0 {
        return;
    }
    error? errors = error("");
    if errors is error {
        return;
    }
    if a is int {
        errors = error("");
    }
}

function testResetTypeNarrowingWithBlockStmt() {
    int a = 10;
    if a > 0 {
        return;
    }
    {
        error? errors = error("");
        if errors is error {
            return;
        }
        if a is int {
            errors = error("");
        }
    }
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError", message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

const globalVar = 2;
int k = let int x = 4 in 2*x*globalVar;

function testBasicLetExpr() {
    int b = let int x = 4 in 2*x*globalVar;
    int c = let int x = 4 in 3*x*globalVar;
    assertTrue(b == 16, "b == 16");
    assertTrue(c == 24, "c == 24");
}

function testBasicLetExprVar() {
    int b = let var x = 4 in 2*x*globalVar;
    assertTrue(b == 16, "b == 16");
}

function testMultipleVarDeclLetExpr() {
    int b = let int x = globalVar*2, int z = 5 in z*x*globalVar;
    assertTrue(b == 40, "b == 40");
}

function testMultipleVarDeclReuseLetExpr() {
    int b = let int x = 2, int z = 5+x in z*x*globalVar;
    assertTrue(b == 28, "b == 28");
}

function testGloballyDefinedLetExpr() {
    assertTrue(k == 16, "b == 16");
}

function testFunctionCallInVarDeclLetExpr() {
    int y = 1;
    int b = let int x = 4, int z = func(y + y*2 + globalVar) in z*(x+globalVar+y);
    assertTrue(b == 70, "b == 70");
}

function testFunctionCallInLetExpr() {
    int y = 1;
    int b = let int x = 4, int z = 10 in func(x*z);
    assertTrue(b == 80, "b == 80");
}

function testLetExprAsFunctionArg() {
    int b = func2(let string x = "aaa", string y = "bbb" in x+y);
    assertTrue(b == 6, "b == 6");
}

function testLetExprInIfStatement() {
    int a = 1;
    if((let int x = 4, int z = 5 in z*x) == 20) {
        a = let int x = 4, int z = 5+a in z+x+a+globalVar;
    }
    assertTrue(a == 13, "a == 13");
}

function testLetExprInWhileStatement() {
    int a = 1;
    while(((let int x = 4, int z = 5 in z*x) == 20) && a == 1) {
        a = let int x = 4, int z = 5+a in z+x+a+globalVar;
    }
    assertTrue(a == 13, "a == 13");
}

function testLetExprInCompoundStatement() {
    int a = 10;
    a -= let int x = 4, int z = 5 in z+x;
    assertTrue(a == 1, "a == 1");
}

function testLetExpressionInMatch() {
    int a = 1;
    match let int x = 4, int z = 5 in z+x {
        1 => {
            a = 2;
        }
        9 => {
            a = 9;
        }
    }
    assertTrue(a == 9, "a == 9");
}

function testLetExpressionInReturn() {
    int result = useLetInReturn();
    assertTrue(result == 9, "result == 9");
}

function testLetExprInElvis() {
    int|() x = ();
    int b;
    b = x ?: let int y = 5, int z = 5 in z+y;
    assertTrue(b == 10, "b == 10");
}

function testLetExprInUnion() {
    int|string x = let int y = 5, int z = 5 in z+y;
    assertTrue(x == 10, "x == 10");
}

function testLetExprInTransaction() {
    int a = 10;
    if (a == 10) {
        int c = 8;
        transaction with retries = 0 {
                int b = let int y = 5+c, int z = 5+a in z+y+a+c+globalVar;
                a = b;
         }
    }
    assertTrue(a == 48, "a == 48");
}

function testLetExprInArrowFunction() {
   int a = 10;
   if (a == 10) {
       int b = 11;
       transaction with retries = 0 {
           int c = a + b;
           function (int, int) returns int ar = (x, y) => let int m = 5+x, int n = 5+y in x+y+m+n+a+b+c+globalVar;
           a = ar(1, 1);
       }
   }
   assertTrue(a == 58, "a == 58");
}

function testLetExprInJSON() {
    json j = {fname:"Jhon", lname:"Doe", age:let int x = 4 in 2*x*globalVar};
    assertTrue(j.age == 16, "j.age == 16");

    json k = {fname:"Jhon", lname:"Doe", age:let json x = {age: 16} in x.age};
    assertTrue(k.age == 16, "k.age == 16");

}

function testLetExpresionInArrays() {
    int[] b = [let int x = globalVar*2, int z = 5 in z*x*globalVar, 2, 3, 4];
    assertTrue(b[0] == 40, "b[0] == 40");

    int[] c = [let int[] x = [2, 3], int z = 5 in z*x[0], 2, 3, 4];
    assertTrue(c[0] == 10, "c[0] == 10");
}

function testLetExpresionInTuples() {
    [int, string] a = [let int x = globalVar*2, int z = 5 in z*x*globalVar, "John"];
    assertTrue(a[0] == 40, "a[0] == 40");

    [int, string] b = [let [int, string] x = [1, "hello"], int z = x[0] + 1 in z*x[0]*globalVar, "John"];
    assertTrue(b[0] == 4, "b[0] == 4");
}

function testLetExprInMap() {
    map<string> addrMap = { line1: let string no = "No, ", string twenty = "20" in no+twenty,
        line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
    assertTrue(addrMap.get("line1") == "No, 20", "addrMap.get(\"line1\") == \"No, 20\"");

    map<string> student = {fname: let map<string> person = {name: "Irshad"} in person.get("name"), lname: "Nilam"};
    assertTrue(student.get("fname") == "Irshad", "student.get(\"fname\") == \"Irshad\"");

}

//type Student record {
//    int marks = let int x = 3, int z = 5 in z*x;
//};

//function testLetExprInRecord() {
//    Student s = {};
//    assertTrue(s.marks == 15, "s.marks == 6");
//}

//type Person object {
//    public int age = let int x = 3, int z = 5 in z*x;
//};

//
//function testLetExprInOBj() {
//    Person s = new;
//    assertTrue(s.age == 15, "s.age == 15");
//}


function useLetInReturn() returns int {
    return let int x = 4, int z = 5 in z+x;
}

function func(int k) returns int {
    return k*2;
}

function func2(string y) returns int {
    return y.length();
}

//// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}

function assertTrue(boolean result, string condition) {
    if (!result) {
        string reason = "condition [" + condition + "] evaluated to 'false'";
        error e = error(reason);
        panic e;
    }
}

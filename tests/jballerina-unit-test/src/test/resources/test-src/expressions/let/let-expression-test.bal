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
int k = let int x = 4 in 2 * x * globalVar;

function testBasicLetExpr() {
    int b = let int x = 4 in 2 * x * globalVar;
    int c = let int x = 4 in 3 * x * globalVar;
    assertTrue(b == 16, "b == 16");
    assertTrue(c == 24, "c == 24");
}

function testBasicLetExprVar() {
    int b = let var x = 4 in 2 * x * globalVar;
    assertTrue(b == 16, "b == 16");
}

function testMultipleVarDeclLetExpr() {
    int b = let int x = globalVar*2, int z = 5 in z * x * globalVar;
    assertTrue(b == 40, "b == 40");
}

function testMultipleVarDeclReuseLetExpr() {
    int b = let int x = 2, int z = 5+x in z * x * globalVar;
    assertTrue(b == 28, "b == 28");
}

function testGloballyDefinedLetExpr() {
    assertTrue(k == 16, "b == 16");
}

function testFunctionCallInVarDeclLetExpr() {
    int y = 1;
    int b = let int x = 4, int z = func(y + y*2 + globalVar) in z * (x + globalVar + y);
    assertTrue(b == 70, "b == 70");
}

function testFunctionCallInLetExpr() {
    int y = 1;
    int b = let int x = 4, int z = 10 in func(x * z);
    assertTrue(b == 80, "b == 80");
}

function testLetExprAsFunctionArg() {
    int b = func2(let string x = "aaa", string y = "bbb" in x+y);
    assertTrue(b == 6, "b == 6");
}

function testLetExprInIfStatement() {
    int a = 1;
    if((let int x = 4, int z = 5 in z*x) == 20) {
        a = let int x = 4, int z = 5+a in z + x + a + globalVar;
    }
    assertTrue(a == 13, "a == 13");
}

function testLetExprInWhileStatement() {
    int a = 1;
    while(((let int x = 4, int z = 5 in z*x) == 20) && a == 1) {
        a = let int x = 4, int z = 5+a in z + x + a + globalVar;
    }
    assertTrue(a == 13, "a == 13");
}

function testLetExprInCompoundStatement() {
    int a = 10;
    a -= let int x = 4, int z = 5 in z + x;
    assertTrue(a == 1, "a == 1");
}

function testLetExpressionInMatch() {
    int a = 1;
    match let int x = 4, int z = 5 in z + x {
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
    b = x ?: let int y = 5, int z = 5 in z + y;
    assertTrue(b == 10, "b == 10");
}

function testLetExprInUnion() {
    int|string x = let int y = 5, int z = 5 in z + y;
    assertTrue(x == 10, "x == 10");
}

function testLetExprInTransaction() {
    int a = 10;
    if (a == 10) {
        int c = 8;
        transaction with retries = 0 {
                int b = let int y = 5 + c, int z = 5 + a in z + y + a + c + globalVar;
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
           function (int, int) returns int ar = (x, y) => let int m = 5 + x, int n = 5 + y
                                                    in x + y + m + n + a + b + c + globalVar;
           a = ar(1, 1);
       }
   }
   assertTrue(a == 58, "a == 58");
}

function testLetExprInJSON() {
    json j = {fname:"Jhon", lname:"Doe", age:let int x = 4 in 2 * x * globalVar};
    assertTrue(j.age == 16, "j.age == 16");

    json k = {fname:"Jhon", lname:"Doe", age:let json x = {age: 16} in x.age};
    assertTrue(k.age == 16, "k.age == 16");

}

function testLetExpresionInArrays() {
    int[] b = [let int x = globalVar*2, int z = 5 in z * x * globalVar, 2, 3, 4];
    assertTrue(b[0] == 40, "b[0] == 40");

    int[] c = [let int[] x = [2, 3], int z = 5 in z*x[0], 2, 3, 4];
    assertTrue(c[0] == 10, "c[0] == 10");
}

function testLetExpresionInTuples() {
    [int, string] a = [let int x = globalVar*2, int z = 5 in z * x * globalVar, "John"];
    assertTrue(a[0] == 40, "a[0] == 40");

    [int, string] b = [let [int, string] x = [1, "hello"], int z = x[0] + 1 in z * x[0] * globalVar, "John"];
    assertTrue(b[0] == 4, "b[0] == 4");
}

function testLetExprInMap() {
    map<string> addrMap = { line1: let string no = "No, ", string twenty = "20" in no+twenty,
        line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
    assertTrue(addrMap.get("line1") == "No, 20", "addrMap.get(\"line1\") == \"No, 20\"");

    map<string> student = {fname: let map<string> person = {name: "Irshad"} in person.get("name"), lname: "Nilam"};
    assertTrue(student.get("fname") == "Irshad", "student.get(\"fname\") == \"Irshad\"");

}

function testLetExpressionTupleSimple() {
    int b = let [boolean, int] t = [true, 4], int x = 2 in t[1] * x;
    assertTrue(b == 8, "b == 8");
}

function testLetExpressionTupleBinding() {
    int b = let [boolean, int] [a1, a2] = [true, 4], int y = a2 * 2 in a2 + y;
    assertTrue(b == 12, "b == 12");
}

function testLetExpressionTupleComplex() {
    int b = let [[string, [int, [boolean, byte]]], [float, int]] v1 =
            [["Ballerina", [3, [true, 34]]], [5.6, 45]], int x = 2 in v1[0][1][0] + x;
    assertTrue(b == 5, "b == 5");
}

function testLetExpressionTupleBindingComplex() {
    int b = let [[string, int], [boolean, float]] [[c1, c2],[c3, c4]] =
            [["Ballerina", 34], [true, 6.7]], int x = 2 in c2 + x;
    assertTrue(b == 36, "b == 36");
}

function testLetExpressionTupleBindingRef() {
    [[string, [int, [boolean, byte]]], [float, int]] v1 = [["Ballerina", [3, [true, 34]]], [5.6, 45]];
    int b = let [[string, [int, [boolean, byte]]], [float, int]] [[d1, [d2, [d3, d4]]], [d5, d6]] = v1, int x = 2
            in  d2 + d4 + x;
    assertTrue(b == 39, "b == 39");
}

function testLetExpressionRecordBindingSimple() {
    int b = let Person { name: firstName, age: personAge, ...otherDetails } = getPerson(), int x = 1 in personAge + x;
    assertTrue(b == 25, "b == 25");
}

function testLetExpressionRecordBindingComplexVar() {
    string city = let var { name: countryName, capital: { name: capitalName } } = getCountry(), string comma = ", "
        in capitalName + comma + countryName;
    assertTrue(city == "Colombo, Sri Lanka", "city == \"Colombo, Sri Lanka\"");
}

function testLetExpressionErrorBindingSimple() {
    int k = let SampleError error(reason, info = info, fatal = fatal) = getSampleError(), int x = 1
        in reason.length() + x;
    assertTrue(k == 13, "k == 13");
}

function testLetExpressionErrorBindingVar() {
    boolean k = let var error(reasonTwo, ...params) = getSampleError() in params["fatal"];
    assertTrue(k, "k == true");
}

function testLetExpressionRecordConstrainedErrorBinding() {
     string msg = let var error(_, detailMsg = detailMsg, isFatal = isFatal) = getRecordConstrainedError() in detailMsg;
     assertTrue(msg == "Failed Message", "msg == \"Failed Message\"");
}

//type Student record {
//    int marks = let int x = 3, int z = 5 in z*x;
//};

//function testLetExprInRecord() {
//    Student s = {};
//    assertTrue(s.marks == 15, "s.marks == 6");
//}

//type Car object {
//    public int year = let int x = 3, int z = 5 in z*x;
//};

//
//function testLetExprInOBj() {
//    Car s = new;
//    assertTrue(s.year == 15, "s.year == 15");
//}


function useLetInReturn() returns int {
    return let int x = 4, int z = 5 in z+x;
}

function func(int k) returns int {
    return k * 2;
}

function func2(string y) returns int {
    return y.length();
}

type Person record {
    string name;
    int age;
    string country;
};

type Country record {
    string name;
    Capital capital;
};

type Capital record {|
    string name;
|};

function getPerson() returns Person {
    Person person = { name: "Irshad", age: 24, country: "Sri Lanka",
                      "occupation": "Software Engineer" };
    return person;
}

function getCountry() returns Country {
    Capital capital = { name: "Colombo" };
    Country country = { name: "Sri Lanka", capital: capital };
    return country;
}

type SampleErrorData record {
    string message?;
    error cause?;
    string info;
    boolean fatal;
};

type SampleError error<string, SampleErrorData>;

function getSampleError() returns SampleError {
    SampleError e = error("Sample Error", info = "Detail Msg", fatal = true);
    return e;
}

type Foo record {|
    string message?;
    error cause?;
    string detailMsg;
    boolean isFatal;
|};

function getRecordConstrainedError() returns error<string, Foo> {
    error<string, Foo> e = error("Some Error", detailMsg = "Failed Message", isFatal = true);
    return e;
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

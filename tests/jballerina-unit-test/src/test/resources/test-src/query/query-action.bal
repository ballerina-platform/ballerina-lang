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

type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type FullName record {|
   string firstName;
   string lastName;
|};

type Department record {|
   string name;
|};

type Employee record {|
   string firstName;
   string lastName;
   string deptAccess;
|};


function testSimpleQueryAction() returns FullName[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x =  from var person in personList
            do {
                FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                nameList[nameList.length()] = fullName;
            };

    return nameList;
}

function testSimpleQueryAction2() returns int{

    int[] intList = [1, 2, 3];
    int count = 0;

    error? x = from var value in intList
            do {
                count += value;
            };

    return count;
}

function testSimpleQueryActionWithRecordVariable() returns FullName[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x = from var { firstName: nm1, lastName: nm2, age: a } in personList
            do {
                FullName fullName = {firstName: nm1, lastName: nm2};
                nameList[nameList.length()] = fullName;
            };

    return  nameList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns FullName[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x = from var { firstName, lastName, age } in personList
            do {
                FullName fullName = {firstName: firstName, lastName: lastName};
                nameList[nameList.length()] = fullName;
            };

    return  nameList;
}

function testSimpleSelectQueryWithLetClause() returns  FullName[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x = from var person in personList
            let int twiceAge  = (person.age * 2)
            do {
                if(twiceAge < 50) {
                    FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                    nameList[nameList.length()] = fullName;
                }

            };
    return  nameList;
}

function testSimpleSelectQueryWithWhereClause() returns  FullName[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    error? x = from var person in personList
            where (person.age * 2) < 50
            do {
                FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                nameList[nameList.length()] = fullName;
            };
    return  nameList;
}

function testSimpleSelectQueryWithMultipleFromClauses() returns  Employee[] {
    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];
    Employee[] employeeList = [];

    error? x = from var person in personList
            from var dept in deptList
            let string hrDepartment = "Human Resource"
            do {
                if(dept.name == "HR") {
                    Employee employee = {firstName: person.firstName, lastName: person.lastName, deptAccess: hrDepartment};
                    employeeList[employeeList.length()] = employee;
                }
            };
    return  employeeList;
}

function testQueryExpressionIteratingOverXMLInFromInQueryAction() returns float {
    xml<xml:Element> bookstore = xml `<bookstore>
                                          <book category="cooking">
                                              <title lang="en">Everyday Italian</title>
                                              <price>30.00</price>
                                          </book>
                                          <book category="children">
                                              <title lang="en">Harry Potter</title>
                                              <price>29.99</price>
                                          </book>
                                          <book category="web">
                                              <title lang="en">XQuery Kick Start</title>
                                              <price>49.99</price>
                                          </book>
                                          <book category="web" cover="paperback">
                                              <title lang="en">Learning XML</title>
                                              <price>39.95</price>
                                          </book>
                                      </bookstore>`;

    float total = 0;
    error? res = from xml:Element price in bookstore/<book>/**/<price>
              do {
                  var p = price/*;
                  if (p is xml:Text) {
                      var i = float:fromString(p.toString());
                      if (i is float) {
                          total += i;
                      }
                  }
              };
    return total;
}

type Record record {|
    int? i;
    string? j;
|};

function testTypeTestInWhereClause() {
    int?[] v = [1, 2, (), 3];
    int total = 0;
    error? result = from var i in v
                    where i is int
                    do {
                        total += i;
                    };
    assertEquality((), result);
    assertEquality(6, total);

    float[] u = [10.5, 20.5, 30.5];
    float x = 0;
    result = from var i in v
             from float j in u
             where i is int
             do {
                 x += <float>i * j;
             };
    assertEquality((), result);
    assertEquality(369.0, x);

    (string|int)[] w = [10, 20, "A", 40, "B"];
    total = 0;
    result = from var i in v
             from int j in (from var k in w where k is int select k)
             where i is int && j > 10
             do {
                 total += i * j;
             };
    assertEquality((), result);
    assertEquality(360, total);

    total = 0;
    result = from var i in v
             where i is int
             where i is 1|2|3
             do {
                 total += i;
             };
    assertEquality((), result);
    assertEquality(6, total);

    Record r1 = {i: 1, j: ()};
    Record r2 = {i: 1, j: "A"};
    Record r3 = {i: 1, j: "C"};

    Record[] recordList = [r1, r2, r3];
    (string|float)[] y = ["X", 30.5, 40.5, "Y", 10.5, "Z", 20.5];
    x = 0;

    result = from var {i, j} in recordList
             from float k in (from var m in y where m is float select m)
             where i is int
             where j is string
             do {
                 x += <float>i * k;
             };
    assertEquality((), result);
    assertEquality(204.0, x);
}

function testTypeNarrowingVarDefinedWithLet() {
    map<error> errorMap = {};
    error? res1 = from int i in 1 ... 2
        let error|string errorOrStr = getErrorOrString()
        where errorOrStr is error
        do {
            errorMap["1"] = errorOrStr;
        };
    error? er1 = errorMap["1"];
    assertEquality((), res1);
    assertEquality(er1 is error, true);

    error? res2 = from int i in 1 ... 2
            let error|string errorOrStr = getErrorOrString()
            do {
                if (errorOrStr is error) {
                  errorMap["2"] = errorOrStr;
                }
            };
    assertEquality((), res2);
    error? er2 = errorMap["2"];
    assertEquality(er2 is error, true);
}

function getErrorOrString() returns error|string {
    return error("Dummy error");
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
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

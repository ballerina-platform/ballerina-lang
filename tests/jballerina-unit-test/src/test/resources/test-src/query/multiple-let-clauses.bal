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
   string deptAccess;
|};

type Company record {|
   string name;
|};

type Teacher record {|
   string firstName;
   string lastName;
   int age;
   string teacherId;
|};

type Department record {|
   int id;
   string name;
|};

type Employee record {|
   string fname;
   string lname;
   int deptId;
|};

function testMultipleLetClausesWithSimpleVariable1() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let string depName = "WSO2", string replaceName = "Alexander"
            where person.deptAccess == "XYZ" && person.firstName == "Alex"
            select {
                   firstName: replaceName,
                   lastName: person.lastName,
                   deptAccess: depName
            };
    return  outputPersonList;
}

function testMultipleLetClausesWithSimpleVariable2() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let string depName = "WSO2"
            let string replaceName = "Alexander"
            where person.deptAccess == "XYZ" && person.firstName == "Alex"
            select {
                   firstName: replaceName,
                   lastName: person.lastName,
                   deptAccess: depName
            };
    return  outputPersonList;
}

function testMultipleLetClausesWithRecordVariable() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, deptAccess: d } in personList
            let Company companyRecord = { name: "WSO2" }
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: companyRecord.name
            };
    return  outputPersonList;
}

function testMultipleVarDeclReuseLetClause() returns Teacher[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};

    Person[] personList = [p1, p2];

    var outputPersonList =
            from var person in personList
            let int x = 20, int y = x + 10
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: y,
                   teacherId: "TER1200"
            };
    return  outputPersonList;
}

function getDept(Person p, stream<Department> dStream) returns Department? {
    foreach var d in dStream {
        if d.name == p.deptAccess {
            return d;
        }
    }
}

function testQueryExpressionWithinLetClause() {
    Person p1 = {deptAccess: "OP", firstName: "Ranjan", lastName: "Fonseka"};
    Person p2 = {deptAccess: "MKT", firstName: "Alex", lastName: "George"};
    Person p3 = {deptAccess: "ENG", firstName: "Grainier", lastName: "Perera"};
    Department d1 = {id: 1, name:"ENG"};
    Department d2 = {id: 2, name:"HR"};
    Department d3 = {id: 3, name:"OP"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2, d3];

    Employee[] empList =
       from var person in personList
       let stream<Department> dS = (stream from var dep in deptList where dep.name != "HR" select dep)
       let Department? d = getDept(person, dS)
       where d != ()
       select {
           fname : person.firstName,
           lname : person.lastName,
           deptId : d.id
       };

    Employee emp;
    any res = empList;
    assertEquality(true, res is Employee[]);
    assertEquality(true, res is (any|error)[]);
    assertEquality(2, empList.length());
    emp = empList[0];
    assertEquality("Ranjan", emp.fname);
    assertEquality("Fonseka", emp.lname);
    assertEquality(3, emp.deptId);
    emp = empList[1];
    assertEquality("Grainier", emp.fname);
    assertEquality("Perera", emp.lname);
    assertEquality(1, emp.deptId);
}

public function testwildcardBindingPatternInLetClause() {

    Person p1 = {deptAccess: "OP", firstName: "Ranjan", lastName: "Fonseka"};
    Person p2 = {deptAccess: "MKT", firstName: "Mark", lastName: "George"};
    Person p3 = {deptAccess: "ENG", firstName: "Grainier", lastName: "Perera"};
    Person[] personList = [p1, p2, p3];
    Person[] outputPersonList = from var person in personList
        let error error(_, msg = message1) = bar()
        let var [_, name] = foo()
        where person.deptAccess == "MKT" && person.firstName == name
        select {
            firstName: name,
            lastName: person.lastName,
            deptAccess: "WSO2"
        };
    assertEquality(1, outputPersonList.length());
    Person p = outputPersonList[0];
    assertEquality(p.firstName, "Mark");
    assertEquality(p.lastName, "George");
}

function foo() returns [string, string] {
  return ["Mark", "Mark"];
}

function bar() returns FooError {
    return error("", msg = "asdasd");
}

type Foo record {
    string msg;
};

type FooError error<Foo>;

//---------------------------------------------------------------------------------------------------------
const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                      message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

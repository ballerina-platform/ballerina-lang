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

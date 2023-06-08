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

type Teacher record {|
   string firstName;
|};

type Department record {|
   string name;
|};

type Student record{|
    string firstName;
    string lastName;
    float score;
|};

type FullName record{|
	string firstName;
	string lastName;
|};

type Person1 record {|
   string firstName;
   string lastName;
   string deptAccess;
   Address address;
|};

type Address record{|
    string city;
    string country;
|};


function testFromClauseWithInvalidType() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from Teacher person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return  outputPersonList;
}

function testFromClauseWithUnDefinedType() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from XYZ person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return  outputPersonList;
}

function testSelectTypeMismatch() {
    Person[] personList = [
        {firstName: "Alex", lastName: "George", age: 23},
        {firstName: "Ranjan", lastName: "Fonseka", age: 30}
    ];

    Teacher[] outputPersonList =
            from Person person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName
            };
}

function testQueryWithInvalidExpressions() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in 10
            where 20
            select 30;

    return  outputPersonList;
}

function testMissingRequiredRecordField() returns Student[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];

	Student[] outputStudentList=
		from var student in studentList
		select {
			firstName: student.firstName,
			score:student.score
		};

    return  outputStudentList;
}

function testInvalidFieldValueInSelect() returns Student[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];

	Student[] outputStudentList=
    	from var student in studentList
    	let int invalidScore=90
    	select {
    		firstName: student.firstName,
    		lastName:student.lastName,
    		score:invalidScore
    	};

    return  outputStudentList;
}

function testUndefinedFunctionInLet() returns Student[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];

    Student[] outputStudentList=
		from var student in studentList
		let float avgScore=calculateScore()
		select {
			firstName: student.firstName,
			lastName:student.lastName,
			score:avgScore
		};

    return  outputStudentList;
}

function testDuplicateKeyInSelect() returns Student[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];

	Student[] outputStudentList=
		from var student in studentList
		select {
		    firstName: student.firstName,
        	lastName:student.lastName,
        	score:student.score,
        	lastName:student.lastName
		};

    return  outputStudentList;
}

function testInvalidRecordBindingPattern() returns Student[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];

    Student[] outputStudentList=
		from var {fname,lastName,score} in studentList
		select {
			firstName: fname,
			lastName: lastName,
			score: score
		};

    return  outputStudentList;
}

function testIncompatibleTypesInFrom() returns Student[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

	Student[] outputStudentList=
		from var student in s1
		select student;

    return  outputStudentList;
}

function testMapAssignmetToRecordTypesWithRequiredFields() returns Person1[]{

    Person1 p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person1 p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

    Person1[] personList = [p1, p2];
	map<string> addr1 = {city:"Manchester",country:"UK"};

    Person1[] outputPersonList=
		from var person in personList
		select {
			firstName: person.firstName,
			lastName: person.lastName,
			deptAccess: person.deptAccess,
			address: addr1
		};

    return  outputPersonList;
}

function testReassignValueInLet() returns FullName[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];
	FullName[] nameList = [];

	var outputNameList =
	    from var student in studentList
	    let float twiceScore = (student.score * 2.0)
	    do {
	        twiceScore = 1000;
	        if(twiceScore < 50.00){
	            FullName fullname = {firstName:student.firstName,lastName:student.lastName};
	            nameList.push(fullname);
	        }
	    };

    return  outputNameList;
}

function testQueryExprForString() returns string {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string outputNameString =
                from var person in personList
                select person.age;

    return outputNameString;
}

function testQueryExprForString2() returns string {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string outputNameString =
                from var person in personList
                select {
                    firstName: person.firstName,
                    lastName: person.lastName
                };

    return outputNameString;
}

function testQueryExprWithAmbigousTypeForXML() {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml|xml[] books = from var x in book/<name>
                select <xml> x;

}

function testQueryExprWithAmbigousTypeForString() {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    string|string[] outputNameString =
                from var person in personList
                select person.firstName;
}

type EmployeeEntity record {
    int id;
    string fname;
    string lname;
    int age;
};

type Employee record {|
    string fname;
    string lname;
    int age;
|};

function testVariableShadowingWithQueryExpressions() {
    string fname = "";
    EmployeeEntity[] entities = [
            {id: 1232, fname: "Sameera", lname: "Jayasoma", age: 30},
            {id: 1232, fname: "Asanthi", lname: "Kulasinghe", age: 30},
            {id: 1232, fname: "Khiana", lname: "Jayasoma", age: 2}
        ];

    Employee[] records = from var {fname, lname, age} in entities select {fname, lname, age};
}

public function testMethodParamWithLet(int age) {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let int age = 35
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: age
             };
}

public function testMethodParamInQuery(int age) {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var {firstName, lastName, age} in personList
            select {
                   firstName: firstName,
                   lastName: lastName,
                   age: age
             };
}

type TableRecord record {
    readonly string name;
    int id;
};

function testTableWithNonMappingType() {

    table<TableRecord> key(name) t = table [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];

    table<int> ids = from var x in t select x.id;
}

function testTableWithNonMappingTypeWithBindingPatterns() {

    table<TableRecord> key(name) t = table [
            {name: "Amy", id: 1234},
            {name: "John", id: 4567}
        ];

    table<int> ids = from var {id} in t select id;
}

public function testInvalidInputType() {
    int x = 1;
    int[] w = from var a in x
                select 1;
}

function testIncompatibleSelectType(stream<string, error?> clientStream) returns error? {
    return from string num in clientStream select {a: 1};
}

function testMapBindingPatternsAnydataType() {
    map<anydata> keyValsMap = {foo:"sss", bar:"ffff"};
    var x = from var {k} in keyValsMap
                 select k;
}

function testMapBindingPatternsAnyType() {
    map<any> keyValsMap = {foo:"sss", bar:"ffff"};
    var x = from var {k} in keyValsMap
                 select k;
}

type User record {
    readonly int id;
    readonly string firstName;
    string lastName;
    int age;
};

function testInvalidTypeInSelectWithQueryConstructingTable() {
    User u1 = {id: 1, firstName: "John", lastName: "Doe", age: 25};
    User u2 = {id: 2, firstName: "Anne", lastName: "Frank", age: 30};

    table<User> key(id) users = table [];
    users.add(u1);
    users.add(u2);

    var result = table key(id) from var user in users
                 where user.age > 21 && user.age < 60
                 select {user};

    User[] userList = [u1, u2];
    result = table key(id) from var user in userList
             where user.age > 21 && user.age < 60
             select {user};
}

function testInvalidTypeInSelectWithQueryConstructingTable2() {
    User u1 = {id: 1, firstName: "John", lastName: "Doe", age: 25};
    User u2 = {id: 2, firstName: "Anne", lastName: "Frank", age: 30};

    table<User> key(id) users = table [];
    users.add(u1);
    users.add(u2);

    var result = table key(id, firstName) from var user in users
                 where user.age > 21 && user.age < 60
                 select {user};

    User[] userList = [u1, u2];
    result = table key(id, firstName) from var user in userList
             where user.age > 21 && user.age < 60
             select {user};
}

type ScoreEvent readonly & record {|
    string email;
    string problemId;
    float score;
|};

type ScoreEventType ScoreEvent;

function testInvalidTypeInFromClause() {
    ScoreEventType[] events = [];

    _ = from int ev in events
        select ev;
}

UndefinedType[] undefinedTypeList = [];

public function testVariableOfUndefinedTypeUsedInFromClause() {
    int[] _ = from var item in undefinedTypeList
            select 1;
}

int[] customerList = [];

public function testVariableOfUndefinedTypeUsedInJoin() {
    int[] _ = from var customer in customerList
        join UndefinedType item in undefinedTypeList
        on 1 equals 1
        select 1;
}

function testInvalidTypeInOnConflictClauseWithQueryConstructingTable() {
    User[] users = [];
    error|int msg = error("Error");

    var result1 = table key(id) from var user in users
                    where user.age > 21 && user.age < 60
                    select {user} on conflict 1;

    var result2 = table key(id) from var user in users
                    where user.age > 21 && user.age < 60
                    select {user} on conflict msg;
}

function testQueryUsedAsFuncArg() {
    int len = getLength(from string s in ["A", "B"]
        select s);

    string[][] ar = [];
    int[][] newAr = from var c in ar
        select foo(from var s in c
            select s.trim());
}

function getLength(int[] arr) returns int {
    return arr.length();
}

function foo(int[] s) returns int[] {
    return s;
}

function testInvalidCheckExpressionInQueryAction() returns string|error {
    from int _ in [1, 3, 5]
    do {
        check returnNil();
        return "string 1";
    };
    return "string 2";
}

function testInvalidCheckExpressionInQueryAction2() returns error? {
    from int _ in [1, 3, 5]
    do {
        check returnNil();
        return;
    };
    return;
}

function returnNil() {
}

type PersonA record {|
    string firstName;
    string lastName;
    int age;
|};

type Persons PersonA[];

function testInvalidContextuallyExpectedTypes() {

    PersonA[] personList = [];
    int outputPersonList =
            from var person in personList
    let int newAge = 20
    where person.age == 33
    select person.firstName;

    Persons outputPersons =
                from var person in personList
    let int newAge = 20
    where person.age == 33
    select person.firstName;

    PersonA outputPerson =
                from var person in personList
    let int newAge = 20
    where person.age == 33
    select person.firstName;
}

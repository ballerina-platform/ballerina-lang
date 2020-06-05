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

function testQueryActionWithMutableParams() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var x = from var person in personList
            do {
                person = {firstName: "XYZ", lastName: "George", age: 30};
            };

    return personList;
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
	    let float twiceScore = (student.score*2)
	    do {
	        twiceScore = 1000;
	        if(twiceScore<50){
	            FullName fullname = {firstName:student.firstName,lastName:student.lastName};
	            nameList.push(fullname);
	        }
	    };

    return  outputNameList;
}

function testQueryExprForXML() returns xml {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml books = from var x in book/<name>
                select x;

    return  books;
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

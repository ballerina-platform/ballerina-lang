type Student record{|
    string firstName;
    string lastName;
    float score;
|};

type FullName record{|
	string firstName;
	string lastName;
|};

type Person record {|
   string firstName;
   string lastName;
   string deptAccess;
   Address address;
|};

type Address record{|
    string city;
    string country;
|};

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

function testMapAssignmetToRecordTypesWithRequiredFields() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

    Person[] personList = [p1, p2];
	map<string> addr1 = {city:"Manchester",country:"UK"};

    Person[] outputPersonList=
		from var person in personList
		select {
			firstName: person.firstName,
			lastName: person.lastName,
			deptAccess: person.deptAccess,
			address: addr1
		};

    return  outputPersonList;
}

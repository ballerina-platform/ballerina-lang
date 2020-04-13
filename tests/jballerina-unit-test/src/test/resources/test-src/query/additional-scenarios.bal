type Person record {|
   string firstName;
   string lastName;
   string deptAccess;
   Address address;
|};

type Department record {|
   string name;
|};

type Student record{|
    string firstName;
    string lastName;
    float score;

|};

type Subscription record{|
    string firstName;
    string lastName;
    float score;
    string degree;
|};

type Address record{|
    string city;
    string country;
|};

type Section record {
   string name;
   Grades grades;
};
//open record
type Grades record{|
    int physics;
    int chemistry;

    int...;
|};

type Teacher record{
	//record type referencing
	*Person;
	Student[] classStudents?;
	//anonymous record type
	record {|
		int duration;
		string qualitifications;
	|} experience;
};

function testMultipleFromAndSelectClausesWithRecordVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Address a1 = {city: "New York", country: "USA"}; //New York
    Address a2 = {city: "Springfield", country: "USA" };//Illinois

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];
    Address[] addressList = [a1, a2];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, deptAccess: d, address: {city:c1, country:c2} } in personList
            from var { name } in deptList
            from var addr in addressList
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: name,
                   address: addr

            };

    return  outputPersonList;
}

function testLogicalOperandsWithWhere() returns Student[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];

    Student[] outputStudentList =
            from var student in studentList
            where student.firstName == "Ranjan" || student.firstName == "Alex" && student.score >= 82.5
            where  student.lastName != "George"
            select {
                   firstName: student.firstName,
                   lastName: student.lastName,
                   score: student.score

            };

    return  outputStudentList;
}

function testQueryExprWithOpenRecords() returns Section[]{

    Section d1={name:"Maths",grades:{physics:80,chemistry:90}};
    Section d2={name:"Bio",grades:{physics:70,chemistry:60}};

    Grades g1={physics:30,chemistry:50,"maths":60};
    Grades g2={physics:50,chemistry:60,"bio":70};

    Section[] sectionList=[d1,d2];
    Grades[] gradesList=[g1,g2];

    Section[] outputSectionmentList=
        from var section in sectionList
        from var grades in gradesList
        let int noOfStudents = 100
        select{
            name: section.name,
            grades:grades,
            "noOfStudents":noOfStudents
        };

    return  outputSectionmentList;
}

function testOthersAssociatedWithRecordTypes() returns Teacher[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Person[] personList=[p1, p2];
    Student[] studentList = [s1, s2];

    Teacher[] outputteacherList=
    	from var person in personList
    	let int period = 10, string degree = "B.Sc."
        select{
    		//change order of the record fields
    		firstName:person.firstName,
    		address: person.address,
    		//optional field
    		classStudents: studentList,
    		deptAccess: person.deptAccess,
    		//member access
    		lastName:person["lastName"],
    		//values for anonymous record fields
    		experience: {
    			duration: period,
    			qualitifications: degree
    		}
    	};

    return  outputteacherList;
}

function testQueryExprTupleTypedBinding1() returns string[]{

    [string,float][] arr1 = [["A",2.0],["B",3.0],["C",4.0]];
    [string|int|float,[string|float, int]][] arr2 = [["Ballerina",[3.4,234]],[34,["APIM",89]],[45.9,[78.2,90]]];

    string[] ouputStringList =
    	from [string,float] [a,b] in arr1
    	from [string|int|float,[string|float, int]] [g1,[g2,g3]] in arr2
    	where g1 == "Ballerina"
    	select a;

    return  ouputStringList;
}

function testQueryExprTupleTypedBinding2() returns int[]{

   	[int,int][] arr1 = [[1,2],[2,3],[3,4]];
    [int,int] arr2 = [1,2];

    int[] ouputList =
    	from [int,int] [a,b] in arr1
    	let [int,int] [d1,d2] = arr2, int x=d1+d2
    	where b > x
    	select a;

    return  ouputList;
}

function testQueryExprRecordTypedBinding() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

	Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

	Person[] outputPersonList =
            from Person { firstName: nm1, lastName: nm2, deptAccess: d, address: a1 } in personList
            from var dept in deptList
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: dept.name,
                   address: a1

            };

    return  outputPersonList;
}

function testQueryExprWithTypeConversion() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

	map<anydata> m = {city:"New York", country:"America"};

	Person[] personList = [p1, p2];

	Person[] outputPersonList=
		from var person in personList
		select{
			firstName: person.firstName,
			lastName: person.lastName,
			deptAccess: person.deptAccess,
			address: <Address>Address.constructFrom(m)
		};

    return  outputPersonList;
}

function testQueryExprWithStreamMapAndFilter() returns Subscription[]{

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];

	Subscription[] outputSubscriptionList =
		from var subs in <stream<Subscription>>studentList.toStream().filter(function (Student student) returns boolean {
			return student.score > 85.3;
			}).'map(function (Student student) returns Subscription {
				Subscription subscription = {
					firstName: student.firstName,
					lastName: student.lastName,
					score: student.score,
					degree: "Bachelor of Medicine"
				};
				return subscription;
				})
		select subs;

	return  outputSubscriptionList;
}
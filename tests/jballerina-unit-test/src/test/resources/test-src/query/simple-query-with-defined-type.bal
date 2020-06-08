type Person record {|
    string firstName;
    string lastName;
    int age;
|};

type Teacher record {|
    string firstName;
    string lastName;
|};

type Employee record {|
    string firstName;
    string lastName;
    string department;
    string company;
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

type Student record{|
    string firstName;
    string lastName;
    float score;
|};

type Teacher1 record{
	//record type referencing
	*Person1;
	Student[] classStudents?;
	//anonymous record type
	record {|
		int duration;
		string qualitifications;
	|} experience;
};

type Subscription record{|
    string firstName;
    string lastName;
    float score;
    string degree;
|};

type NumberGenerator object {
    int i = 0;
    public function next() returns record {|int value;|}|error? {
        //closes the stream after 5 events
        if (self.i == 5) {
            return ();
        }
        self.i += 1;
        return {value: self.i};
    }
};

type NumberGeneratorWithError object {
    int i = 0;
    public function next() returns record {|int value;|}|error? {
        if (self.i == 3) {
            return error("Custom error thrown explicitly.");
        }
        self.i += 1;
        return {value: self.i};
    }
};

type ResultValue record {|
    Person value;
|};

function getRecordValue((record {| Person value; |}|error?)|(record {| Person value; |}?) returnedVal) returns Person? {
    if (returnedVal is ResultValue) {
        return returnedVal.value;
    } else {
        return ();
    }
}

function testSimpleSelectQueryWithSimpleVariable() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithRecordVariable() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, age: a } in personList
            select {
                   firstName: nm1,
                   lastName: nm2,
                   age: a
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var { firstName, lastName, age } in personList
            select {
                   firstName: firstName,
                   lastName: lastName,
                   age: age
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithRecordVariableV3() returns Teacher[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Teacher[] outputPersonList =
            from var { firstName, lastName, age } in personList
            select {
                   firstName,
                   lastName
            };

    return outputPersonList;
}

function testSimpleSelectQueryWithWhereClause() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            where person.age >= 30
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };
    return outputPersonList;
}

function testQueryExpressionForPrimitiveType() returns boolean {

    int[] intList = [12, 13, 14, 15, 20, 21, 25];

    int[] outputIntList =
            from var value in intList
            where value > 20
            select value;

    return outputIntList == [21, 25];
}

function testQueryExpressionWithSelectExpression() returns boolean {

    int[] intList = [1, 2, 3];

    string[] stringOutput =
            from var value in intList
            select value.toString();

    return stringOutput == ["1","2", "3"];
}

function testFilteringNullElements() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};

    Person?[] personList = [p1, (), p2];

    Person[] outputPersonList =
                     from var person in personList
                     where (person is Person)
                     select {
                         firstName: person.firstName,
                         lastName: person.lastName,
                         age: person.age
                         };
    return outputPersonList;
}

function testMapWithArity() returns boolean {
    map<any> m = {a: "1A", b: "2B", c: "3C", d: "4D"};
    string[] val = from var v in m
                   where <string> v == "1A"
                   select <string> v;
    return val == ["1A"];
}

function testJSONArrayWithArity() returns boolean {
    json[] jdata = [{name: "bob", age: 10}, {name: "tom", age: 16}];
    string[] val = from var v in jdata
                   select <string> v.name;
    return val == ["bob", "tom"];
}

function testArrayWithTuple() returns boolean {
    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];
    string[] val = from var [i, v] in arr
                   where i == 3
                   select v;
    return val == ["C"];
}

function testFromClauseWithStream() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", age: 30};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 40};
    Person p3 = {firstName: "John", lastName: "David", age: 50};

    Person[] personList = [p1, p2, p3];
    stream<Person> streamedPersons = personList.toStream();

    Person[] outputPersonList =
            from var person in streamedPersons
            where person.age == 40
            select person;
    return outputPersonList;
}

function testSimpleSelectQueryWithLetClause() returns Employee[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Employee[] outputPersonList =
            from var person in personList
            let string depName = "HR", string companyName = "WSO2"
            where person.age >= 30
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   department: depName,
                   company: companyName
            };
    return outputPersonList;
}

function testFunctionCallInVarDeclLetClause() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};

    Person[] personList = [p1, p2];

    var outputPersonList =
            from Person person in personList
            let int twiceAge = mutiplyBy2(person.age)
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: twiceAge
            };

    return outputPersonList;
}

function testUseOfLetInWhereClause() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 18};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 22};

    Person[] personList = [p1, p2];

    var outputPersonList =
            from var person in personList
            let int twiceAge = mutiplyBy2(person.age)
            where twiceAge > 40
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: twiceAge
            };

    return outputPersonList;
}

function mutiplyBy2(int k) returns int {
    return k * 2;
}

public function testQueryWithStream() returns boolean {
    NumberGenerator numGen = new;
    var numberStream = new stream<int, error>(numGen);

    int[]|error oddNumberList = from int num in numberStream
                                 where (num % 2 == 1)
                                 select num;
    return oddNumberList == [1, 3, 5];
}

public function testQueryStreamWithError() returns int[]|error {
    NumberGeneratorWithError numGen = new;
    var numberStream = new stream<int, error>(numGen);

    int[]|error oddNumberList = from int num in numberStream
                                where (num % 2 == 1)
                                select num;
    return oddNumberList;
}

function testOthersAssociatedWithRecordTypes() returns Teacher1[]{

    Person1 p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person1 p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Person1[] personList = [p1, p2];
    Student[] studentList = [s1, s2];

    Teacher1[] outputteacherList =
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

function testQueryExprTupleTypedBinding2() returns boolean {

   	[int,int][] arr1 = [[1,2],[2,3],[3,4]];
    [int,int] arr2 = [1,2];

    int[] ouputList =
    	from [int,int] [a,b] in arr1
    	let [int,int] [d1,d2] = arr2, int x=d1+d2
    	where b > x
    	select a;

    return  ouputList == [3];
}

function testQueryExprWithTypeConversion() returns Person1[]{

    Person1 p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person1 p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

	map<anydata> m = {city:"New York", country:"America"};

	Person1[] personList = [p1, p2];

	Person1[] outputPersonList =
		from var person in personList
		select{
			firstName: person.firstName,
			lastName: person.lastName,
			deptAccess: person.deptAccess,
			address: <Address> m.cloneWithType(Address)
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

function testSimpleSelectQueryReturnStream() returns boolean {
    boolean testPassed = true;
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    stream<Person> outputPersonStream = stream from var person in personList
                                select {
                                    firstName: person.firstName,
                                    lastName: person.lastName,
                                    age: person.age
                                };
    Person? returnedVal = getRecordValue(outputPersonStream.next());
    testPassed = testPassed && (returnedVal is Person) && (returnedVal == p1);

    returnedVal = getRecordValue(outputPersonStream.next());
    testPassed = testPassed && (returnedVal is Person) && (returnedVal == p2);

    returnedVal = getRecordValue(outputPersonStream.next());
    testPassed = testPassed && (returnedVal is Person) && (returnedVal == p3);
    return testPassed;
}

function testQueryWithRecordVarInLetClause() returns Person1[] {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Address address = {city: "Colombo", country: "SL"};

    Person[] personList = [p1, p2, p3];

    var outputPersonList = from var person in personList
                           let Address {city: town, country: state } = address
                           where person.age >= 30
                           select {
                               firstName: person.firstName,
                               lastName: person.lastName,
                               deptAccess: "XYZ",
                               address: {city: town, country: state}
                           };
    return outputPersonList;
}

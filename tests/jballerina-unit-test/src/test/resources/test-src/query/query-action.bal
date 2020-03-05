type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type FullName record {|
   string firstName;
   string lastName;
|};

function testSimpleQueryAction() returns FullName[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    from var person in personList
    do {
            FullName fullName = {firstName: person.firstName, lastName: person.lastName};
            nameList[nameList.length()] = fullName;
    }

    return nameList;
}

function testSimpleQueryAction2() returns int{

    int[] intList = [1, 2, 3];
    int count = 0;

    from var value in intList
    do {
        count += value;
    }

    return count;
}

function testSimpleQueryActionWithRecordVariable() returns FullName[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    from var { firstName: nm1, lastName: nm2, age: a } in personList
    do {
        FullName fullName = {firstName: nm1, lastName: nm2};
        nameList[nameList.length()] = fullName;
    }

    return  nameList;
}

function testSimpleSelectQueryWithRecordVariableV2() returns FullName[]{

    Person p1 = {firstName:"Alex", lastName: "George", age: 23};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName:"John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    from var { firstName, lastName, age } in personList
    do {
        FullName fullName = {firstName: firstName, lastName: lastName};
        nameList[nameList.length()] = fullName;
    }

    return  nameList;
}

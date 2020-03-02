type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type Department record {|
   string name;
|};

function testMultiplefromClauseWithTypeStream() returns Person[]{
    Person p1 = {firstName: "Alex", lastName: "George", age: 30};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 40};
    Person p3 = {firstName: "John", lastName: "David", age: 50};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] departmentList = [d1, d2];

    Person[] outputPersonList =
            from var person in personList.toStream()
            from var department in departmentList.toStream()
            where person.age == 40 && department.name == "HR"
            select person;
    return  outputPersonList;
}

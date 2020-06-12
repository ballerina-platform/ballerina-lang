type DeptPerson record {|
   string fname;
   string lname;
   string dept;
|};

type Department record {|
   int id;
   string name;
|};

type Person record {|
   int id;
   string fname;
   string lname;
|};

function testJoinClauseWithInvalidType() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join Person dept in deptList
       on person.id equals dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    return deptPersonList;
}

function testJoinClauseWithUndefinedType() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join XYZ dept in deptList
       on person.id equals dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    return deptPersonList;
}

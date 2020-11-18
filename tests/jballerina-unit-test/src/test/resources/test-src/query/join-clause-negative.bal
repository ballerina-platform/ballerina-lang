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

function testSimpleJoinWithInvalidEqualsClause1() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join var {id: deptId, name: deptName} in deptList
       on deptId equals person.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : deptName
       };

    return deptPersonList;
}

function testSimpleJoinWithInvalidEqualsClause2() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join var {id,name} in deptList
       on id equals person.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function testSimpleJoinWithInvalidEqualsClause3() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       let string deptName = "HR"
       join var {id,name} in deptList
       on name equals deptName
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function testOuterJoinWithInvalidEqualsClause1() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var {id: deptId, name: deptName} in deptList
       on deptId equals person.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : deptName
       };

    return deptPersonList;
}

function testOuterJoinWithInvalidEqualsClause2() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join var {id,name} in deptList
       on id equals person.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function testOuterJoinWithInvalidEqualsClause3() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       let string deptName = "HR"
       outer join var {id,name} in deptList
       on name equals deptName
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function testOuterJoinWithInvalidEqualsClause4() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       let string deptName = "HR"
       join var {id,name} in deptList
       on getDeptName(id) equals deptName
       select {
           fname : person.fname,
           lname : person.lname,
           dept : name
       };

    return deptPersonList;
}

function getDeptName(int id) returns string {
    if (id == 1) {
        return "HR";
    } else {
        return "Operations";
    }
}

function testOnClauseWithFunction() returns DeptPerson[]{
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join Department dept in deptList
       on condition(person.fname)
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };

    return deptPersonList;
}

function testOuterJoinWithOnClauseWithFunction() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join Department dept in deptList
       on condition(person.fname)
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };
}

function testOuterJoinWithOutOnClause() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join Department dept in deptList
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };
}

function testOnClauseWithoutEquals() returns boolean {
    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       outer join Department dept in deptList
       on person.id == dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : dept.name
       };
}

function condition(string name) returns boolean{
    return name == "Alex";
}

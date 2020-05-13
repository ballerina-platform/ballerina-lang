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

type DeptPersonValue record {|
    DeptPerson value;
|};

function getDeptPersonValue((record {| DeptPerson value; |}|error?)|(record {| DeptPerson value; |}?) returnedVal) returns DeptPersonValue? {
    var result = returnedVal;
    if (result is DeptPersonValue) {
        return result;
    } else {
        return ();
    }
}


function testSimpleJoinClause() returns DeptPerson[]{
    boolean testPassed = true;

    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    DeptPerson[] deptPersonList =
       from var person in personList
       join Department dept in deptList
       on person.id equals dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : "Eng"
       };

    return deptPersonList;
}

function testJoinClauseWithStream() returns boolean {
    boolean testPassed = true;

    Person p1 = {id: 1, fname: "Alex", lname: "George"};
    Person p2 = {id: 2, fname: "Ranjan", lname: "Fonseka"};

    Department d1 = {id: 1, name:"HR"};
    Department d2 = {id: 2, name:"Operations"};

    Person[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

    stream<Person> personStream = personList.toStream();
    stream<Department> deptStream = deptList.toStream();

    stream<DeptPerson> deptPersonStream =
       stream from var person in personStream
       join var dept in deptStream
       on person.id equals dept.id
       select {
           fname : person.fname,
           lname : person.lname,
           dept : "Eng"
       };

    record {| DeptPerson value; |}? deptPerson = getDeptPersonValue(deptPersonStream.next());
    testPassed = testPassed && deptPerson?.value?.fname == "Alex" && deptPerson?.value?.lname == "George"
                             && deptPerson?.value?.dept == "Eng";

    deptPerson = getDeptPersonValue(deptPersonStream.next());
    testPassed = testPassed && deptPerson?.value?.fname == "Ranjan" && deptPerson?.value?.lname == "Fonseka"
                                 && deptPerson?.value?.dept == "Eng";

    return testPassed;
}



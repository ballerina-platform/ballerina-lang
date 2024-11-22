type Person record {|
    readonly int id;
    string name;
    int age?;
    Person parent?;
|};

type Student record {
    int id;
    string fname;
    string lname;
    int age;
    float gpa;
};

function getStudents() returns Student[] {
    Student s3 = {id: 3, fname: "Amy", lname: "Melina", age: 30, gpa: 1.3};
    Student s1 = {id: 1, fname: "Jon", lname: "Doe", age: 21, gpa: 2.1};
    Student s2 = {id: 2, fname: "Jane", lname: "Doe", age: 25, gpa: 3.2};
    return [s1, s2, s3];
}

function getPeople() returns Person[] {
    Person p1 = {id: 1, name: "Jon Doe"};
    Person p2 = {id: 2, name: "Jane Doe"};
    return [p1, p2];
}

function testQueryExprWithJoin() {
    return from var st in getStudents()
        join var {id, name} in getPeople() on st.id equals id
        select {name: name, gpa: st.gpa};
}

function testQueryExprWithOnConflict() {
    return table key(id) from var st in getStudents().toStream()
        select <Person>{id: st.id, name: st.fname, age: st.age}
        on conflict error("Conflicted Key", cKey = st.id);
}

function testQueryExprWithGroupBy() {
    return from var {name, age} in getPeople()
        group by age
        select age;
}

function testQueryExprWithCollect() {
    return from var {age, gpa} in getStudents()
        let var ageScore = (50 - age) * gpa
        collect sum(ageScore);
}

public function main() {
    Employee[] empList =
        from var person in personList
    let stream<Department> dS = stream from var dep in deptList
        where dep.name != "HR"
        select dep
    let Department? d = getDept(person, dS)
    where d != ()
    select {
        fname: person.firstName,
        lname: person.lastName,
        deptId: d.id
    };
}

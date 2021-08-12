public function main() {
    models:PET[] listResult = from var pet in allpets
        where pet.value > 1000
        w //<cursor>
        select pet;

    DeptPerson[] deptPersonList =
            from var person in personList
    join var {id: deptId, name: deptName} in deptList
            on person.id equals deptId
    w //<cursor>
    select {
        fname: person.fname,
        lname: person.lname,
        dept: deptName
    };
}

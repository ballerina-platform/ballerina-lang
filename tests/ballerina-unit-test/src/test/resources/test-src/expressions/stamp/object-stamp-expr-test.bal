
type PersonObj object {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
};

type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};

//----------------------------Object Stamp -------------------------------------------------------------


function stampObjectsV1() returns EmployeeObj {
    PersonObj p = new PersonObj();
    EmployeeObj employee = p.stamp(EmployeeObj);

    return employee;
}

function stampObjectsToAny() returns any {
    PersonObj p = new PersonObj();
    any anyValue = p.stamp(any);

    return anyValue;
}



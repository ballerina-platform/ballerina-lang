type DepartmentLT record {
    string dptName = "";
    PersonLT[] employees = [];
    PersonLT manager = {};
};

type PersonLT record {
    string name = "default first name";
    string lname = "";
    map<any> adrs = {};
    int age = 999;
    PersonLT? child = ();
};

function testStructLiteral1 () returns (DepartmentLT) {
    DepartmentLT p = {};
    return p;
}

function testStructLiteral2 () returns (PersonLT) {
    PersonLT p = {};
    return p;
}


public function testObjectBasedQueryExpr() {
    testQueryExprForObject();
}

type Student object {
    public string firstName;
    public string lastName;
    public int age;
    string address;

    function init(string firstName, string lastName, int age, string address = "No 20, Palm grove") {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
        self.address = address;
    }

    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }
};

public function testQueryExprForObject() {

    Student p1 = new ("Alex", "George", 23, "Kandy");
    Student p2 = new ("Ranjan", "Fonseka", 30);
    Student p3 = new ("John", "David", 33);

    Student[] studentList = [p1, p2, p3];

    Student[] outputStudentList =
            from var student in studentList
            select new (student.firstName, student.lastName, student.age, student.address);

    assertEquality("No 20, Palm grove", outputStudentList[1].address);
    assertEquality("Kandy", outputStudentList[0].address);
}


//---------------------------------------------------------------------------------------------------------
type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

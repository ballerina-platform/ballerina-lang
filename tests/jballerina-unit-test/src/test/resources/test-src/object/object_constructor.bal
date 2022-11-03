int a = 0;

class Person {

    function init() {
        a += 1;
    }
}

public function testObjectInit() returns int {
    Person p;
    p = new;
    return a;
}

function testLocalVariablesAssignmentToFields() {
    int a = 10;

    var obj = object {
        int x = a;

        function init() {
            assertEquality(self.x, 10);
        }
    };
    assertEquality(obj.x, 10);
}

isolated function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}

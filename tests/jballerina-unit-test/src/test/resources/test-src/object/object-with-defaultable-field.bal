
public function testObjectWithSimpleInit () returns [int, string, int, string] {
    Person p = new Person(99);
    return [p.age, p.name, p.year, p.month];
}

class Person {
    public int age = 10;
    public string name = "";

    int year = 0;
    string month = "february";

    function init (int count, int year = 50, string name = "sample value1", string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age = self.age + count;
        self.month = val1;
    }
}

final int classI = 111222;

class ModuleVariableReferencingClass {
    int i = classI;
}

function value(int k = classI) returns int {
    return k;
}

ModuleVariableReferencingClass c1 = new;

function testClassWithModuleLevelVarAsDefaultValue() {
    ModuleVariableReferencingClass c = new;
    assertEquality(111222, c.i);
    assertEquality(111222, c1.i);
}

function testObjectConstructorWithModuleLevelVarAsDefaultValue() {
    var value = object {
        int i = classI;
    };
    assertEquality(111222, value.i);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

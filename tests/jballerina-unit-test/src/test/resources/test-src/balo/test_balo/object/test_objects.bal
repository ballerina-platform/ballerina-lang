import testorg/foo;
import testorg/utils;

public function testSimpleObjectAsStruct () returns [int, string, int, string] {
    foo:Man p = new ();
    return [p.age, p.name, p.year, p.month];
}

public function testObjectFieldDefaultable () returns [int, string, int, string] {
    foo:Man p = new foo:Man();
    return [p.age, p.name, p.year, p.month];
}

public function testSimpleObjectAsStructWithNew () returns [int, string, int, string] {
    foo:Man p = new;
    return [p.age, p.name, p.year, p.month];
}

public function testObjectWithSimpleInit () returns [int, string, int, string] {
    foo:Human p = new foo:Human(99, 7);
    return [p.age, p.name, p.year, p.month];
}

public function testObjectWithDefaultableValues () returns [int, string, int, string] {
    foo:Planet p = new foo:Planet(99);
    return [p.age, p.name, p.year, p.month];
}

public function testObjectWithSimpleInitWithDiffValues () returns [int, string, int, string] {
    foo:Human p = new foo:Human(675, 27, val1 = "adding value in invocation");
    return [p.age, p.name, p.year, p.month];
}

public function testObjectWithoutRHSType () returns [int, string, int, string] {
    foo:Human p = new (675, 27, val1 = "adding value in invocation");
    return [p.age, p.name, p.year, p.month];
}

public function testObjectWithAttachedFunc1 () returns [int, string, int, string] {
    foo:Company p = new foo:Company(99, 7);
    var [a, b] = p.attachFunc1(344, "added values ");
    return [a, b, p.year, p.month];
}

public function testObjectWithSelfKeyword () returns [string, string, string, string] {
    foo:Building p = new foo:Building();
    return [p.getNameWrapperInside1(), p.getNameWrapperInside2(), p.getNameWrapperOutside1(), p.getNameWrapperOutside2()];
}

public function testObjectCallAttachedFunctions () returns [string, string, string, string] {
    foo:Building p = new foo:Building();
    return [p.getNameWrapperInside1(), p.getNameWrapperInside2(), p.getNameWrapperOutside1(), p.getNameWrapperOutside2()];
}

public function testObjectInsideObject () returns [string, string] {
    foo:Boy p = new foo:Boy();
    return [p.getNameWrapperInside1(), p.getNameFromDiffObject()];
}

function testGetValueFromPassedSelf() returns string {
    foo:Boy p = new foo:Boy();
    return p.selfAsValue();
}

public function testObjectWithInterface () returns [int, string, int, string] {
    foo:Bin p = new DustBin(100, 10, val1 = "adding value in invocation");
    return [p.age, p.name, p.year, p.month];
}

public type DustBin object {
    public int age = 20;
    public string name = "sample name";
    public int year = 50;
    public string month = "february";

    public function __init (int year, int count, string name = "sample value1", string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age = self.age + count + 50;
        self.month = val1 + " uuuu";
    }

    public function attachFunc1(int add, string value1) returns [int, string] {
        int count = self.age + add;
        string val2 = value1 + self.month;
        return [count, val2];
    }

    public function attachInterface(int add, string value1) returns [int, string] {
        int count = self.age + add;
        string val2 = value1 + self.month;
        return [count, val2];
    }
};

public function testShadowingObjectField () returns [int, string] {
    foo:Car p = new foo:Car(a = 50, n = "passed in name value");
    return [p.age, p.name];
}

function testCreateObjectInReturnSameType () returns int {
    return returnSameObectInit().age;
}

function testCreateObjectInReturnDifferentType () returns int {
    return returnDifferentObectInit().age;
}

function returnSameObectInit() returns foo:Girl {
    return new (5);
}

function returnDifferentObectInit() returns foo:Girl {
    return new Women(5, 7);
}

public type Women object {
    public int age;

    public function __init (int age, int addVal) {
        self.age = age + addVal;
    }
};

type Vehicle object {
    public int age = 0;
    public string name = "";
    public foo:Bus emp = new;
    public foo:Tyre foo = new;
    public foo:Wheel bar = new;
};

Vehicle v = new;

function testGetDefaultValuesInObjectGlobalVar() returns [int, string, int, string] {
    return [v.age, v.emp.name, v.foo.key, v.bar.address];
}

function testGetDefaultValuesInObject() returns [int, string, int, string] {
    Vehicle p = new;
    return [p.age, p.emp.name, p.foo.key, p.bar.address];
}

function testCyclicReferenceWithDefaultable () returns int {
    foo:Tiger p = new();
    foo:Cat em = new;
    em.age = 89;
    p.emp = em;
    var emp = p.emp;
    if (emp is foo:Cat) {
        return emp.age;
    }
    return 56;
}

function testRecursiveObjectWithNill() returns int {
    foo:Bird p = new;
    return (p.age);
}

public type Office object {
    public int age = 90;
    public foo:Architect ep = new(88, "sanjiva");
};

function testFieldWithExpr() returns [int, string] {
    Office p = new;
    return [p.ep.pp, p.ep.name];
}

function testObjectWithByteTypeFields() returns [byte[], byte[], byte[]] {
    foo:Desk desk = new;
    return [desk.dimensions, desk.code1, desk.code2];
}

// --------------Test abstract objects and object type reference -----------------

public function testObjectReferingTypeFromBalo_1() returns [string, float] {
    foo:Manager1 mgr = new();
    return [mgr.getName(), mgr.getBonus(0.1)];
}

// Test referring an object coming from a balo 
type Manager2 object {
    string dpt = "HR";

    *foo:Employee2;

    function __init(string name, int age=25) {
        self.name = name;
        self.age = age;
        self.salary = 3000.0;
    }

    public function getBonus(float ratio, int months=6) returns float {
        return self.salary*ratio*months;
    }

    public function getName(string greeting = "Hello") returns string {
        return greeting + " " + self.name;
    }
};

public function testObjectReferingTypeFromBalo_2() returns [string, float] {
    Manager2 mgr2 = new("Jane");
    return [mgr2.getName(), mgr2.getBonus(0.1)];
}

// Test referring a type coming from a balo
type Employee3 abstract object {
    public float salary;
    *foo:Person1;

    public function getBonus(float ratio, int months=10) returns float;
};

// Test invking a method with default values, of an object
// coming from a balo 
type Manager3 object {
    string dpt = "HR";

    *Employee3;

    function __init(string name, int age=25) {
        self.name = name;
        self.age = age;
        self.salary = 3000.0;
    }

    public function getBonus(float ratio, int months=6) returns float {
        return self.salary*ratio*months;
    }

    public function getName(string greeting = "Good morning") returns string {
        return greeting + " " + self.name;
    }
};

public function testObjectReferingTypeFromBalo_3() returns [string, float] {
    Manager3 mgr3 = new("Jane");
    return [mgr3.getName(), mgr3.getBonus(0.1)];
}

public function testObjectReferingNonAbstractObjFromBalo() {
    foo:CorronifiedEmployee cemp1 = new (true, 100.0, 200.3, 20, "John");
    utils:assertEquality("Engineering", cemp1.dpt);
    utils:assertEquality(500.3, cemp1.setWorkingFromHomeAllowance(500.3));
    utils:assertEquality(600.3, cemp1.getBonus(1.0, 1));
    cemp1.setWorkingFromHome(false);
    utils:assertEquality(100.0, cemp1.getBonus(1.0, 1));
    utils:assertEquality((), cemp1.setWorkingFromHomeAllowance(50));
    utils:assertEquality("Contactless hello! John", cemp1.getName());
    utils:assertEquality(20, cemp1.Age());
}

public type PostPandemicEmployee object {
    *foo:CorronifiedEmployee;

    public function __init(boolean workingFromHome, float salary, float workingFromHomeAllowance, int age, string name) {
        self.age                        = age;
        self.name                       = name;
        self.workingFromHome            = workingFromHome;
        self.salary                     = salary;
        self.workingFromHomeAllowance   = workingFromHomeAllowance;
        self.dpt                        = "Engineering";
    }

    public function setWorkingFromHomeAllowance(float allowance) returns float|() {
        self.workingFromHomeAllowance = allowance;
        if (self.workingFromHome) {
            return self.workingFromHomeAllowance;
        }
        return ();
    }

    public function setWorkingFromHome(boolean workingFromHome) {
        self.workingFromHome = workingFromHome;
    }

    public function getBonus(float ratio, int months=12) returns float {
        if (self.workingFromHome) {
            return self.salary * ratio * months + self.workingFromHomeAllowance;
        }
        return self.salary * ratio * months;
    }

    public function getName(string greeting = "Contactless hello!") returns string {
        return greeting + " " + self.name;
    }

    public function Age() returns int {
        return self.age;
    }
};

public function testObjectReferingNonAbstractObjLoadedFromBalo() {
    PostPandemicEmployee cemp1 = new (true, 100.0, 200.3, 20, "John");
    utils:assertEquality("Engineering", cemp1.dpt);
    utils:assertEquality(500.3, cemp1.setWorkingFromHomeAllowance(500.3));
    utils:assertEquality(600.3, cemp1.getBonus(1.0, 1));
    cemp1.setWorkingFromHome(false);
    utils:assertEquality(100.0, cemp1.getBonus(1.0, 1));
    utils:assertEquality((), cemp1.setWorkingFromHomeAllowance(50));
    utils:assertEquality("Contactless hello! John", cemp1.getName());
    utils:assertEquality(20, cemp1.Age());
}

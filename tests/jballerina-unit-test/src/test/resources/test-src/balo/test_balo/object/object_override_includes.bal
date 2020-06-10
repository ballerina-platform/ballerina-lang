import testorg/foo;

public type NewButSameBin object {
    *foo:Bin;
    public int age;
    public string name;
    public int year;
    public string month;

    public function attachFunc1(int add, string value1) returns [int, string] {
        return [2, "dummy"];
    }

    public function attachInterface(int add, string value1) returns [int, string] {
        return [2, "dummy"];
    }

    public function __init(int age, string name, int year, string month) {
        self.age = age;
        self.name = name;
        self.year = year;
        self.month = month;
    }
};

public function testSimpleObjectOverridingSimilarObject () {
    NewButSameBin p = new (20, "Bob", 2020, "march");
    assertEquality(20, p.age);
    assertEquality("Bob", p.name);
    assertEquality(2020, p.year);
    assertEquality("march", p.month);
}

public type DustBinOverridingBin object {
    public int age = 20;
    public string name = "sample name";
    public int year = 50;
    public string month = "february";

    *foo:Bin;

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

public function testObjectOverrideInterfaceWithInterface() {
    foo:Bin p = new DustBinOverridingBin(100, 10, val1 = "adding");
    assertEquality(80, p.age);
    assertEquality("sample value1", p.name);
    assertEquality(100, p.year);
    assertEquality("adding uuuu", p.month);
}

public function testObjectWithOverriddenFieldsAndMethods() {
    foo:ManagingDirector p = new foo:ManagingDirector(20, "John");
    assertEquality(2000000, p.getBonus(1, 1));
    assertEquality("Hello Director, John", p.getName());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

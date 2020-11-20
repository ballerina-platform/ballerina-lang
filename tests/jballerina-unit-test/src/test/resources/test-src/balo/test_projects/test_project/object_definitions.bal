
public class Man {
    public int age = 10;
    public string name = "sample name";
    public int year = 50;
    public string month = "february";
}

public class Human {
    public int age = 10;
    public string name = "sample name";
    public int year = 50;
    public string month = "february";

    public function init (int year, int count, string name = "sample value1", string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age += count;
        self.month = val1;
    }
}

public class Planet {
    public int age = 10;
    public string name;
    public int year;
    public string month = "february";

    public function init (int count, int year = 50, string name = "sample value1", string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age += count;
        self.month = val1;
    }
}

public class Company {
    public int age = 10;
    public string name = "sample name";
    public int year = 50;
    public string month = "february";

    public function init (int year, int count, string name = "sample value1", string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age += count;
    }

    public function attachFunc1(int add, string value1) returns [int, string] {
        int count = self.age + add;
        string val2 = value1 + self.month;
        return [count, val2];
    }
}

public class Building {
    public int age = 10;
    public string name = "sample name";

    private int year = 50;
    private string month = "february";

    public function getName() returns string {
        return self.name;
    }

    public function getNameWrapperInside1() returns string {
        return self.getName();
    }

    public function getNameWrapperInside2() returns string {
        return self.getNameOut();
    }

    public function getNameOut() returns string {
        return self.name;
    }

    public function getNameWrapperOutside1() returns string {
        return self.getName();
    }

    public function getNameWrapperOutside2() returns string {
        return self.getNameOut();
    }
}

public class Boy {
    public int age = 10;
    public string name = "sample name";

    private int year = 50;
    private string month = "february";

    public function getName() returns string {
        return self.name;
    }

    public function getNameWrapperInside1() returns string {
        return self.getName();
    }

    public function getNameFromDiffObject() returns string {
        Boy p = new ();
        p.name = "changed value";
        return p.getName();
    }

    public function selfAsValue() returns string {
        return passSelfAsValue(self);
    }
}

function passSelfAsValue(Boy p) returns string {
    return p.getName();
}

public type Bin object {
    public int age;
    public string name;
    public int year;
    public string month;

    public function attachFunc1(int add, string value1) returns [int, string];

    public function attachInterface(int add, string value1) returns [int, string];
};

public class Car {
    public int age = 10;
    public string name;

    public function init (int a = 10, string n = "sample result") {
        self.age = a;
        self.name = n;
    }
}

public class Girl {
    public int age;

    public function init (int age) {
        self.age = age;
    }
}

public class Bus {
    public int age;
    public string name;

    public isolated function init (int age = 6, string key = "abc") {
        self.age = age;
        self.name = "sample value";
    }
}

public class Tyre {
    public int key = 0;
    public string value = "";

    public isolated function init () {

    }
}

public class Wheel {
    public string address = "";
}

public class Tiger {
    public int age = 0;
    public Cat? emp = ();
}

public class Cat {
    public int age = 0;
    public Dog? foo = ();
    public Lion? bar = ();
}

public class Dog {
    public int calc = 0;
    public Lion? bar1 = ();
}

public type Lion record {
    int barVal;
    string name;
    Tiger? person;
};

public class Bird {
    public int age = 90;
    public Parrot ep = new();
}

public class Parrot {
    public int pp = 0;
    public Bird? p = ();
}

public class Architect {
    public int pp;
    public string name;

    public isolated function init (int pp, string name) {
        self.pp = pp;
        self.name = name;
    }
}

public type Country object {
    public int age;
    public string month;

    public function attachInterface(int add) returns int;
};

public class House {
    public int age;

    public function init (int age) {
        self.age = age;
    }
}

public class Apartment {
    public int age;

    public function init (int age, int addVal) {
        self.age = age;
        self.age += addVal;
    }
}

public class Desk {

    public int length = 23;
    public int width = 12;
    public float height = 4.5;
    public byte[] dimensions = [3,4,5,8];
    public byte[] code1 = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    public byte[] code2 = base16 `aaabcfccad afcd34 1a4bdf abcd8912df`;

    public function init () {
    }
}

public class ObjectWithModuleLevelVisibilityField {
    public int i;
    boolean b;

    public function init(int i, boolean b) {
        self.i = i;
        self.b = b;
    }

    public function getInt() returns int {
        return self.i;
    }
}

public class ObjectWithModuleLevelVisibilityMethod {
    public int i;
    public boolean b;

    public function init(int i, boolean b) {
        self.i = i;
        self.b = b;
    }

    function getInt() returns int {
        return self.i;
    }
}

public class ObjectWithPublicFieldsAndMethods {
    public int i;
    public boolean b;

    public function init(int i, boolean b = true) {
        self.i = i;
        self.b = b;
    }

    public function getInt() returns int {
        return self.i;
    }
}

public function getObjectWithModuleLevelVisibilityField() returns ObjectWithModuleLevelVisibilityField => new (1, true);

public function getObjectWithModuleLevelVisibilityMethod() returns ObjectWithModuleLevelVisibilityMethod =>
    new (2, false);

public function getObjectWithPublicFieldsAndMethods() returns ObjectWithPublicFieldsAndMethods => new (3);

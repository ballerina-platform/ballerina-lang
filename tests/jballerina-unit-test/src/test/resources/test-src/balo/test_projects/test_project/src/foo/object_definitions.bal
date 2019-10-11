
public type Man object {
    public int age = 10;
    public string name = "sample name";
    public int year = 50;
    public string month = "february";
};

public type Human object {
    public int age = 10;
    public string name = "sample name";
    public int year = 50;
    public string month = "february";

    public function __init (int year, int count, public string name = "sample value1", public string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age += count;
        self.month = val1;
    }
};

public type Planet object {
    public int age = 10;
    public string name;
    public int year;
    public string month = "february";

    public function __init (int count, int year = 50, public string name = "sample value1", public string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age += count;
        self.month = val1;
    }
};

public type Company object {
    public int age = 10;
    public string name = "sample name";
    public int year = 50;
    public string month = "february";

    public function __init (int year, int count, public string name = "sample value1", public string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age += count;
    }

    public function attachFunc1(int add, string value1) returns [int, string] {
        int count = self.age + add;
        string val2 = value1 + self.month;
        return [count, val2];
    }
};

public type Building object {
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
};

public type Boy object {
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
};

function passSelfAsValue(Boy p) returns string {
    return p.getName();
}

public type Bin abstract object {
    public int age;
    public string name;
    public int year;
    public string month;

    public function attachFunc1(int add, string value1) returns [int, string];

    public function attachInterface(int add, string value1) returns [int, string];
};

public type Car object {
    public int age = 10;
    public string name;

    public function __init (public int a = 10, public string n = "sample result") {
        self.age = a;
        self.name = n;
    }
};

public type Girl object {
    public int age;

    public function __init (int age) {
        self.age = age;
    }
};

public type Bus object {
    public int age;
    public string name;

    public function __init (public int age = 6, public string key = "abc") {
        self.age = age;
        self.name = "sample value";
    }
};

public type Tyre object {
    public int key = 0;
    public string value = "";

    public function __init () {

    }
};

public type Wheel object {
    public string address = "";
};

public type Tiger object {
    public int age = 0;
    public Cat? emp = ();
};

public type Cat object {
    public int age = 0;
    public Dog? foo = ();
    public Lion? bar = ();
};

public type Dog object {
    public int calc = 0;
    public Lion? bar1 = ();
};

public type Lion record {
    int barVal;
    string name;
    Tiger? person;
};

public type Bird object {
    public int age = 90;
    public Parrot ep = new();
};

public type Parrot object {
    public int pp = 0;
    public Bird? p = ();
};

public type Architect object {
    public int pp;
    public string name;

    public function __init (int pp, string name) {
        self.pp = pp;
        self.name = name;
    }
};

public type Country abstract object {
    public int age;
    public string month;

    public function attachInterface(int add) returns int;
};

public type House object {
    public int age;

    public function __init (int age) {
        self.age = age;
    }
};

public type Apartment object {
    public int age;

    public function __init (int age, int addVal) {
        self.age = age;
        self.age += addVal;
    }
};

public type Desk object {

    public int length = 23;
    public int width = 12;
    public float height = 4.5;
    public byte[] dimensions = [3,4,5,8];
    public byte[] code1 = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    public byte[] code2 = base16 `aaabcfccad afcd34 1a4bdf abcd8912df`;

    public function __init () {
    }
};

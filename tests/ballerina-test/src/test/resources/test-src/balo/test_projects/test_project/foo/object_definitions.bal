
public type Man object {
    public {
        int age = 10,
        string name = "sample name";
        int year = 50;
        string month = "february";
    }
};

public type Human object {
    public {
        int age = 10,
        string name = "sample name";
        int year = 50;
        string month = "february";
    }

    public new (year, int count, name = "sample value1", string val1 = "default value") {
        age = age + count;
        month = val1;
    }
};

public type Planet object {
    public {
        int age = 10,
        string name;
        int year;
        string month = "february";
    }

    public new (year = 50, int count, name = "sample value1", string val1 = "default value") {
        age = age + count;
        month = val1;
    }
};

public type Company object {
    public {
        int age = 10,
        string name = "sample name";
        int year = 50;
        string month = "february";
    }

    public new (year, int count, name = "sample value1", string val1 = "default value") {
        age = age + count;
    }

    public function attachFunc1(int add, string value1) returns (int, string) {
        int count = age + add;
        string val2 = value1 + month;
        return (count, val2);
    }
};

public type Building object {
    public {
        int age = 10,
        string name = "sample name";
    }
    private {
        int year = 50;
        string month = "february";
    }

    public function getName() returns string {
        return name;
    }

    public function getNameWrapperInside1() returns string {
        return self.getName();
    }

    public function getNameWrapperInside2() returns string {
        return self.getNameOut();
    }

    public function getNameOut() returns string;

    public function getNameWrapperOutside1() returns string;

    public function getNameWrapperOutside2() returns string;

};

public function Building::getNameOut() returns string {
    return self.name;
}

public function Building::getNameWrapperOutside1() returns string {
    return self.getName();
}

public function Building::getNameWrapperOutside2() returns string {
    return self.getNameOut();
}

public type Boy object {
    public {
        int age = 10,
        string name = "sample name";
    }
    private {
        int year = 50;
        string month = "february";
    }

    public function getName() returns string {
        return name;
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

public type Bin object {
    public {
        int age = 10,
        string name = "sample name";
        int year = 50;
        string month = "february";
    }

    public new (year, int count, name = "sample value1", string val1 = "default value") {
        age = age + count;
        month = val1;
    }

    public function attachFunc1(int add, string value1) returns (int, string) {
        int count = age + add;
        string val2 = value1 + month;
        return (count, val2);
    }

    public function attachInterface(int add, string value1) returns (int, string);
};

public type Car object {
    public {
        int age = 10,
        string name,
    }

    public new (int a = 10, string n = "sample result") {
        self.age = a;
        self.name = n;
    }
};

public type Girl object {
    public {
        int age,
    }

    public new (age) {

    }
};

public type Bus object {
    public {
        int age,
        string name,
    }

    public new (age = 6, string key = "abc") {
        name = "sample value";
    }
};

public type Tyre object {
    public {
        int key,
        string value,
    }
    public new () {

    }
};

public type Wheel object {
    public {
        string address,
    }

};

public type Tiger object {
    public {
        int age,
        Cat? emp,
    }
};

public type Cat object {
    public {
        int age,
        Dog? foo,
        Lion? bar,
    }
};

public type Dog object {
    public {
        int calc,
        Lion? bar1,
    }
};

public type Lion record {
    int barVal,
    string name,
    Tiger? person,
};

public type Bird object {
    public {
        int age = 90,
        Parrot ep,
    }
};

public type Parrot object {
    public {
        int pp,
        Bird? p,
    }
};

public type Architect object {
    public {
        int pp,
        string name,
    }

    public new (pp, name) {

    }
};

public type Country object {
    public {
        int age = 10,
        string month = "february";
    }

    public function attachInterface(int add) returns int;
};

public type House object {
    public {
        int age,
    }

    public new (age) {

    }
};

public type Apartment object {
    public {
        int age,
    }

    public new (age, int addVal) {
        age = age + addVal;
    }
};

public type Desk object {

    public {
        int length = 23,
        int width = 12,
        float height = 4.5,
        byte[] dimensions = [3,4,5,8],
        byte[] code1 = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`,
        byte[] code2 = base16 `aaabcfccad afcd34 1a4bdf abcd8912df`;
    }

    public new (){

    }
};


public function testInitAbstractObject ()  {
    Person1 p1 = new Person1();
    Person2 p2 = new Person2();
}

public function testInitAbstractObjectWithNew () {
    Person1 p1 = new;
    Person2 p2 = new;
}

type Person1 object {
    public int age;
    public string name;

    int year;
    string month;
};

// Abstract object with constructor method
type Person2 object {
    public int age;
    public string name;

    int year;
    string month;

    function init () {
    }
};

// Non-abstract object with a function that has no implementation
class Person3 {
    public int age = 0;
    public string name = "";

    int year = 0;
    string month = "";

    public function getName() returns string;
}

// Abstract object with method definition
type Person4 object {
    public int age;
    public string name;

    int year;
    string month;

    public function getName() returns string {
        return "name";
    }
};

// Abstract object with private field
type Person6 object {
    private int age;
    public string name;

    private function getName() returns string;
};

type Foo object {
    private function getName() returns string;
};

class Bar {
    *Foo;

    private function getName() returns string {
        return "bar";
    }
}

// Abstract object with extern method
type Person7 object {
    public function getName() returns string = external;
};

// Abstract object with field with default value.
type Baz object {
    string xyz = "xyz";
};

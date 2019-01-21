
public function testInitAbstractObject ()  {
    Person1 p1 = new Person1();
    Person2 p2 = new Person2();
}

public function testInitAbstractObjectWithNew () {
    Person1 p1 = new;
    Person2 p2 = new;
}

type Person1 abstract object {
    public int age = 0;
    public string name = "";

    int year = 0;
    string month = "";
};

// Abstract object with constructor method
type Person2 abstract object {
    public int age = 0;
    public string name = "";

    int year = 50;
    string month = "february";

    function __init () {
    }
};

// Non-abstract object with a function that has no implementation
type Person3 object {
    public int age = 0;
    public string name = "";

    int year = 0;
    string month = "";

    public function getName() returns string;
};

// Abstract object with method definition
type Person4 abstract object {
    public int age = 0;
    public string name = "";

    int year = 0;
    string month = "";

    public function getName() returns string {
        return self.name;
    }
};

// Attach function to an abstract object
type Person5 abstract object {
    public int age = 0;
    public string name = "";

    int year = 0;
    string month = "";

    public function getName() returns string;
};

public function Person5.getName() returns string {
    return "my name";
}

// Abstract object with private field
type Person6 abstract object {
    private int age = 0;
    public string name = "";

    private function getName() returns string;
};

type Foo abstract object {
    private function getName() returns string;
};

type Bar object {
    *Foo;
};

private function Bar.getName() returns string {
    return "bar";
}

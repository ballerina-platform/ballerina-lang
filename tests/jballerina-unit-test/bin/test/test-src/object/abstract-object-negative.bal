
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

    function init(int age, string name, int year, string month);
};

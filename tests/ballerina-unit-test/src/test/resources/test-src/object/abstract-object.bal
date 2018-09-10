
public function testInitAbstractObject ()  {
    Person1 p1 = new Person1();
    Person2 p2 = new Person2();
}

public function testInitAbstractObjectWithNew () {
    Person1 p1 = new;
    Person2 p2 = new;
}

type Person1 abstract object {
    public int age;
    public string name;

    int year;
    string month;

};

type Person2 abstract object {
    public int age;
    public string name;

    int year = 50;
    string month = "february";
    
    new () {
    }
};

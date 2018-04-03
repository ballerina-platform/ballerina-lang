
public function testObjectWithInterface () returns (int, string, int, string) {
    Person p = new Employee(100, 10, val1 = "adding value in invocation");
    return (p.age, p.name, p.year, p.month);
}


type Person object {
    public {
        int age = 10,
        string name = "sample name";
    }
    private {
        int year = 50;
        string month = "february";
    }

    new (year, int count, name = "sample value1", string val1 = "default value") {
        age = age + count;
        month = val1;
    }

    function attachFunc1(int add, string value1) returns (int, string) {
        int count = age + add;
        string val2 = value1 + month;
        return (count, val2);
    }

    function attachInterface(int add, string value1) returns (int, string);
}

type Employee object {
    public {
        int age = 20,
        string name = "sample name";
    }
    private {
        int year = 50;
        string month = "february";
    }

    new (year, int count, name = "sample value1", string val1 = "default value") {
        age = age + count + 50;
        month = val1 + " uuuu";
    }

    function attachFunc1(int add, string value1) returns (int, string) {
        int count = age + add;
        string val2 = value1 + month;
        return (count, val2);
    }

    function attachInterface(int add, string value1) returns (int, string){
        int count = age + add;
        string val2 = value1 + month;
        return (count, val2);
    }
}


